package dev.soffa.foundation.pubsub.rabbitmq;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.message.Message;
import dev.soffa.foundation.message.MessageHandler;
import dev.soffa.foundation.message.pubsub.PubSubClient;
import dev.soffa.foundation.message.pubsub.PubSubClientConfig;
import dev.soffa.foundation.pubsub.AbstractPubSubClient;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.soffa.foundation.pubsub.rabbitmq.AmqpUtil.TOPIC;

public class AmqpClient extends AbstractPubSubClient implements PubSubClient {

    private static final Logger LOG = Logger.get(AmqpClient.class);
    private final PubSubClientConfig config;

    private final RabbitAdmin rabbitAdmin;
    private final List<SimpleMessageListenerContainer> listeners = new ArrayList<>();

    public AmqpClient(String applicationName, PubSubClientConfig config, String broadcasting) {
        super(applicationName, config, broadcasting);
        this.config = config;
        this.rabbitAdmin = configure();
    }

    @Override
    protected CompletableFuture<byte[]> sendAndReceive(@NonNull String subject, Message message) {
        return CompletableFuture.supplyAsync(() -> {
            org.springframework.amqp.core.Message msg = new org.springframework.amqp.core.Message(
                Mappers.JSON.serializeAsBytes(message)
            );
            org.springframework.amqp.core.Message res = this.rabbitAdmin.getRabbitTemplate().sendAndReceive(msg);
            if (res == null || res.getBody() == null) {
                return null;
            }
            return res.getBody();
        });
    }


    @Override
    public void subscribe(MessageHandler handler) {
        SimpleMessageListenerContainer container = AmqpUtil.createListener(
            rabbitAdmin.getRabbitTemplate(), applicationName, handler, config.getOption("mode")
        );
        container.setMessageListener(message -> {
            //EL
            handler.handle(Mappers.JSON.deserialize(message.getBody(), Message.class));
            LOG.info("Message processed: %s", message.getMessageProperties().getDeliveryTag());
        });
        listeners.add(container);
        container.start();

    }

    @Override
    public void publish(@NonNull String subject, Message message) {
        this.rabbitAdmin.getRabbitTemplate().convertAndSend(
            subject,
            "", Mappers.JSON.serialize(message));
    }

    @Override
    public void publish(Message message) {
        publish(applicationName + TOPIC, message);
    }

    @Override
    public void broadcast(@NonNull String target, Message message) {
        this.rabbitAdmin.getRabbitTemplate().convertAndSend(target, Mappers.JSON.serialize(message));
    }

    private RabbitAdmin configure() {
        return AmqpUtil.configure(applicationName, config);
    }

    @PreDestroy
    protected void destroy() {
        listeners.forEach(SimpleMessageListenerContainer::destroy);
    }


}
