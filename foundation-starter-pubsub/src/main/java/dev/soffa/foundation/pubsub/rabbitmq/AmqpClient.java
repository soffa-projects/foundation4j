package dev.soffa.foundation.pubsub.rabbitmq;

import com.google.common.base.Preconditions;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.message.Message;
import dev.soffa.foundation.message.MessageHandler;
import dev.soffa.foundation.message.MessageResponse;
import dev.soffa.foundation.message.pubsub.PubSubClient;
import dev.soffa.foundation.message.pubsub.PubSubClientConfig;
import dev.soffa.foundation.pubsub.AbstractPubSubClient;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.HandlerAdapter;
import org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AmqpClient extends AbstractPubSubClient implements PubSubClient {

    private final PubSubClientConfig config;
    private final RabbitAdmin rabbitAdmin;
    private final List<AbstractMessageListenerContainer> listeners = new ArrayList<>();
    private final boolean embedded;


    @SneakyThrows
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
                Mappers.JSON_DEFAULT.serializeAsBytes(message)
            );
            msg.getMessageProperties().setAppId(applicationName);
            msg.getMessageProperties().setCorrelationId(TextUtil.snakeCase(message.getOperation()) + "_");
            // msg.getMessageProperties().setReplyTo(DefaultIdGenerator.uuid(TextUtil.snakeCase(applicationName)));
            org.springframework.amqp.core.Message res = this.rabbitAdmin.getRabbitTemplate().sendAndReceive(
                subject, "", msg
            );
            if (res == null || res.getBody() == null) {
                return null;
            }
            return res.getBody();
        });
    }

    @SneakyThrows
    @Override
    public void subscribe(@NonNull String subject, boolean broadcast, MessageHandler messageHandler) {
        registerSubscription(subject);

        if (broadcast && embedded) {
            AmqpUtil.createFanoutExchange(rabbitAdmin, subject, applicationName);
            return;
        } else if (embedded) {
            AmqpUtil.declarExchange(rabbitAdmin, subject, subject);
        }

        AbstractMessageListenerContainer container = AmqpUtil.createListener(
            rabbitAdmin.getRabbitTemplate(), subject, null, config.getOption("mode")
        );

        Object bean = new InternalMessageHandler(messageHandler);
        MessagingMessageListenerAdapter listener = new MessagingMessageListenerAdapter(bean, InternalMessageHandler.HANDLER);
        listener.setHandlerAdapter(new HandlerAdapter(new InvocableHandlerMethod(bean, InternalMessageHandler.HANDLER)));
        Preconditions.checkNotNull(listener);
        container.setMessageListener(listener);
        listeners.add(container);
        container.start();
    }

    @Override
    public void subscribe(MessageHandler handler) {
        if (!hasSubscription(applicationName)) {
            subscribe(applicationName, false, handler);
        } else {
            Logger.platform.warn("A subscription already exists for: %s", applicationName);
        }
    }

    @Override
    public void publish(@NonNull String subject, Message message) {
        this.rabbitAdmin.getRabbitTemplate().convertAndSend(
            subject,
            "", Mappers.JSON_DEFAULT.serialize(message));
    }

    @Override
    public void publish(Message message) {
        publish(applicationName, message);
    }

    @Override
    public void broadcast(@NonNull String target, Message message) {
        this.rabbitAdmin.getRabbitTemplate().convertAndSend(target, Mappers.JSON_DEFAULT.serialize(message));
    }

    private RabbitAdmin configure() {
        return AmqpUtil.configure(applicationName, config);
    }

    @PreDestroy
    protected void destroy() {
        listeners.forEach(AbstractMessageListenerContainer::destroy);
    }

    @AllArgsConstructor
    static class InternalMessageHandler {

        public static final Method HANDLER;

        static {
            try {
                HANDLER = InternalMessageHandler.class.getMethod("handle", org.springframework.amqp.core.Message.class);
            } catch (NoSuchMethodException e) {
                throw new TechnicalException(e);
            }
        }

        private MessageHandler handler;

        public String handle(org.springframework.amqp.core.Message message) {
            boolean hasReply = TextUtil.isNotEmpty(message.getMessageProperties().getReplyTo());
            try {
                Object result = handler.handle(Mappers.JSON_DEFAULT.deserialize(message.getBody(), Message.class)).orElse(null);
                if (hasReply) {
                    return Mappers.JSON_DEFAULT.serialize(MessageResponse.ok(result));
                }
                return null;
            } catch (Exception e) {
                if (hasReply) {
                    return Mappers.JSON_DEFAULT.serialize(MessageResponse.error(e));
                } else {
                    throw e;
                }
            }
        }

    }


}
