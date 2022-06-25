package dev.soffa.foundation.pubsub.nats;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.error.ManagedException;
import dev.soffa.foundation.message.MessageResponse;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class NatsMessageHandler implements MessageHandler {

    private static final Logger LOG = Logger.get(NatsMessageHandler.class);

    private final Connection connection;
    private final dev.soffa.foundation.message.MessageHandler handler;

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
        dev.soffa.foundation.message.Message message;

        try {
            message = Mappers.JSON.deserialize(msg.getData(), dev.soffa.foundation.message.Message.class);
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
                    MessageResponse response = MessageResponse.ok(operationResult.orElse(null));
                    LOG.debug("Sending response back to %s [SID:%s]", msg.getReplyTo(), msg.getSID());
                    connection.publish(msg.getReplyTo(), msg.getSubject(), Mappers.JSON.serializeAsBytes(response));
                }
            }
        } catch (Exception e) {
            LOG.error("Nats event handling failed with error", e);
            if (e instanceof ManagedException) {
                if (sendReply) {
                    connection.publish(
                        msg.getReplyTo(),
                        msg.getSubject(),
                        Mappers.JSON.serializeAsBytes(MessageResponse.error(e))
                    );
                }
            } else {
                throw e;
            }
        }
    }


}
