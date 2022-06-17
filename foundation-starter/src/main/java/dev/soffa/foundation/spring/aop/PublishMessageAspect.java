package dev.soffa.foundation.spring.aop;

import dev.soffa.foundation.annotation.Publish;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.config.AppConfig;
import dev.soffa.foundation.message.Message;
import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.message.pubsub.PubSubClient;
import dev.soffa.foundation.model.ResponseEntity;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PublishMessageAspect {

    private static final Logger LOG = Logger.get(PublishMessageAspect.class);
    private final PubSubClient pubSub;
    private final AppConfig config;

    public PublishMessageAspect(AppConfig config,
                                @Autowired(required = false) PubSubClient pubSub
    ) {
        this.pubSub = pubSub;
        this.config = config;
    }

    @SneakyThrows
    @Around("@annotation(publish)")
    public Object publishMessage(ProceedingJoinPoint pjp, Publish publish) {
        Object result = pjp.proceed(pjp.getArgs());
        if (pubSub == null) {
            LOG.warn("Unable to honor @Publish annotation because no PubSubClient is registered");
        } else {
            try {
                String event = publish.event();
                String subject = publish.target();
                Object payload = result;
                if (result instanceof ResponseEntity<?>) {
                    payload = ((ResponseEntity<?>) result).getData();
                }
                Message msg = MessageFactory.create(event, payload);
                if (msg.getContext() != null) {
                    msg.getContext().setAuthorization(null);
                }
                if (Publish.SELF_TARGET_1.equalsIgnoreCase(subject) || Publish.SELF_TARGET_2.equalsIgnoreCase(subject)) {
                    pubSub.publish(config.getName(), msg);
                } else if (Publish.BROADCAST_TARGET.equalsIgnoreCase(subject)) {
                    pubSub.broadcast(msg);
                } else {
                    pubSub.publish(subject, msg);
                }
                LOG.info("Message dispatched: %s -> %s [handled by %s]", event, subject, pubSub.getClass().getSimpleName());
            } catch (Exception e) {
                LOG.error(e, "Failed to publish message %s -- %s", publish.event(), e.getMessage());
                //TODO: we should requeue the message and retry later
            }
        }
        return result;
    }


}
