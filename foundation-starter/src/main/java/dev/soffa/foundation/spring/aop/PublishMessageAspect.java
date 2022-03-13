package dev.soffa.foundation.spring.aop;

import dev.soffa.foundation.annotations.Publish;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.messages.Message;
import dev.soffa.foundation.messages.MessageFactory;
import dev.soffa.foundation.messages.pubsub.PubSubClient;
import dev.soffa.foundation.models.ResponseEntity;
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

    public PublishMessageAspect(@Autowired(required = false) PubSubClient pubSub) {
        this.pubSub = pubSub;
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
                if ("*".equalsIgnoreCase(subject)) {
                    pubSub.broadcast(subject, msg);
                } else {
                    pubSub.publish(subject, msg);
                }
                LOG.info("Message dispatched: %s", event);
            } catch (Exception e) {
                LOG.error(e, "Failed to publish message %s -- %s", publish.event(), e.getMessage());
                //TODO: we should requeue the message and retry later
            }
        }
        return result;
    }


}
