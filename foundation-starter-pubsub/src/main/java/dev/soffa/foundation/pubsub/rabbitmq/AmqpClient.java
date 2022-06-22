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
    private final boolean embedded;

    public AmqpClient(String applicationName, PubSubClientConfig config, String broadcasting) {
        super(applicationName, config, broadcasting);
        this.config = config;
        this.rabbitAdmin = configure();
        this.embedded = config.getAddresses().contains("://embedded");
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
    public void subscribe(@NonNull String subject, boolean broadcast, MessageHandler messageHandler) {

        if (broadcast && embedded) {
            AmqpUtil.createFanoutExchange(rabbitAdmin, subject, applicationName);
            return;
        } else if (embedded) {
            AmqpUtil.declareQueue(rabbitAdmin, subject);
        }
        SimpleMessageListenerContainer container = AmqpUtil.createListener(
            rabbitAdmin.getRabbitTemplate(), subject, null, config.getOption("mode")
        );
        container.setMessageListener(message -> {
            //EL
            messageHandler.handle(Mappers.JSON.deserialize(message.getBody(), Message.class));
            LOG.info("Message processed: %s", message.getMessageProperties().getDeliveryTag());
        });
        listeners.add(container);
        container.start();
    }

    @Override
    public void subscribe(MessageHandler handler) {
        subscribe(applicationName, false, handler);
    }

    @Override
    public void publish(@NonNull String subject, Message message) {
        String target = subject;
        if (applicationName.equals(target)) {
            // Message should go to TOPIC exchange
            target = target + TOPIC;
        }
        LOG.info("[amqp] Publishing message to %s", target);
        this.rabbitAdmin.getRabbitTemplate().convertAndSend(
            target,
            "", Mappers.JSON.serialize(message));
    }

    @Override
    public void publish(Message message) {
        publish(applicationName, message);
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
