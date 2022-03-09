package dev.soffa.foundation.pubsub.nats;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.ObjectUtil;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.errors.ManagedException;
import dev.soffa.foundation.models.CallResult;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class NatsMessageHandler implements MessageHandler {

    private static final Logger LOG = Logger.get(NatsMessageHandler.class);

    private final Connection connection;
    private final dev.soffa.foundation.pubsub.MessageHandler handler;

    private boolean accept(Message msg) {
        if (msg == null) {
            return false;
        }
        if (msg.isStatusMessage()) {
            return false;
        }
        return msg.getData() != null;
    }

    @Override
    public void onMessage(Message msg) {
        if (!accept(msg)) {
            return;
        }
        boolean sendReply = !msg.isJetStream() && TextUtil.isNotEmpty(msg.getReplyTo());
        LOG.info("Message received: SID=%s Jetstream:%s", msg.getSID(), msg.isJetStream());

        dev.soffa.foundation.messages.Message message;

        try {
            message = ObjectUtil.deserialize(msg.getData(), dev.soffa.foundation.messages.Message.class);
        } catch (Exception e) {
            //TODO: handle lost payloads (audit)
            LOG.error(e, "Invalid payload, message will be discarded -- %s", e.getMessage());
            return;
        }

        if (message == null) {
            return;
        }

        try {
            Optional<Object> operationResult = handler.handle(message);
            if (operationResult.isPresent() && sendReply) {
                Object result = operationResult.get();
                Class<?> clazz = result.getClass();
                boolean isNoop = "kotlin.Unit".equalsIgnoreCase(clazz.getName()) || clazz == Void.class;
                if (!isNoop) {
                    CallResult response = CallResult.create(operationResult.orElse(null), null);
                    LOG.debug("Sending response back to %s [SID:%s]", msg.getReplyTo(), msg.getSID());
                    connection.publish(msg.getReplyTo(), msg.getSubject(), ObjectUtil.serialize(response));
                }
            }
            LOG.info("Message SID=%s processed with no error", msg.getSID());
        } catch (Exception e) {
            LOG.error("Nats event handling failed with error", e);
            if (e instanceof ManagedException) {
                if (sendReply) {
                    connection.publish(msg.getReplyTo(), msg.getSubject(), ObjectUtil.serialize(CallResult.create(null, e)));
                }
            } else {
                throw e;
            }
        }
    }


}
