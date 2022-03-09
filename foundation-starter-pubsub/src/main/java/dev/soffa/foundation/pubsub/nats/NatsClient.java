package dev.soffa.foundation.pubsub.nats;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.errors.TechnicalException;
import dev.soffa.foundation.messages.Message;
import dev.soffa.foundation.messages.MessageHandler;
import dev.soffa.foundation.messages.pubsub.PubSubClient;
import dev.soffa.foundation.messages.pubsub.PubSubClientConfig;
import dev.soffa.foundation.pubsub.AbstractPubSubClient;
import io.nats.client.*;
import io.nats.client.api.AckPolicy;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.PublishAck;
import io.nats.client.api.StreamConfiguration;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;


public class NatsClient extends AbstractPubSubClient implements PubSubClient {

    private static final Logger LOG = Logger.get(NatsClient.class);
    private final PubSubClientConfig config;

    private Connection connection;
    private JetStream stream;


    public NatsClient(String applicationName, PubSubClientConfig config, String broadcasting) {
        super(applicationName, config, broadcasting);
        this.config = config;
        configure();
    }


    @SneakyThrows
    @Override
    public void subscribe(@NonNull String subject, boolean broadcast, MessageHandler messageHandler) {
        LOG.info("Configuring subscription to %s", subject);

        NatsMessageHandler h = new NatsMessageHandler(connection, messageHandler);
        @SuppressWarnings("PMD")
        Dispatcher dispatcher = connection.createDispatcher();

        if (!broadcast) {
            dispatcher.subscribe(subject, subject + "-group", h);
        } else {
            configureStream(subject);
            ConsumerConfiguration c = ConsumerConfiguration.builder()
                .durable(applicationName)
                .ackWait(Duration.ofSeconds(5))
                .ackPolicy(AckPolicy.Explicit)
                .build();
            PushSubscribeOptions pso = PushSubscribeOptions.builder().configuration(c).build();
            stream.subscribe(subject, dispatcher, h, true, pso);
        }
    }

    private void configure() {
        try {
            String[] addresses = config.getAddresses().split(",");
            Options o = new Options.Builder().servers(addresses)
                .maxReconnects(-1).build();
            connection = Nats.connect(o);
            // connection.flush(Duration.ofSeconds(1));
            JetStreamOptions jso = JetStreamOptions.defaultOptions();
            this.stream = connection.jetStream(jso);
            LOG.info("Connected to NATS servers: %s", config.getAddresses());
        } catch (Exception e) {
            NatsUtil.close(connection);
            throw new TechnicalException(e, "Unable to connect to NATS @ %s", config.getAddresses());
        }
    }

    @SneakyThrows
    private void configureStream(@NonNull String subject) {

        JetStreamManagement jsm = connection.jetStreamManagement();
        try {
            StreamConfiguration.Builder scBuilder = StreamConfiguration.builder()
                .name(subject)
                .addSubjects(subject);
            jsm.addStream(scBuilder.build());
        } catch (JetStreamApiException ignore) {
            LOG.warn("Stream %s already configured", subject);
        }
    }

    @Override
    public CompletableFuture<byte[]> internalRequest(@NonNull String subject, Message message) {
        return connection.request(NatsUtil.createNatsMessage(subject, message)).thenApply(io.nats.client.Message::getData);
    }

    @Override
    public void publish(@NonNull String target, @NonNull Message message) {
        connection.publish(NatsUtil.createNatsMessage(target, message));
    }

    @SneakyThrows
    @Override
    public void broadcast(@NonNull String target, @NonNull Message message) {
        String sub = resolveBroadcast(target);
        if (TextUtil.isEmpty(sub)) {
            LOG.warn("Broadcasting ignored: %s, target is empy.", message.getOperation());
            return;
        }
        PublishAck ack = stream.publish(NatsUtil.createNatsMessage(sub, message));
        if (ack.hasError()) {
            throw new TechnicalException(ack.getError());
        }
    }

    @PreDestroy
    @SuppressWarnings("PMD")
    protected void cleanup() {
        NatsUtil.close(connection);
    }

}
