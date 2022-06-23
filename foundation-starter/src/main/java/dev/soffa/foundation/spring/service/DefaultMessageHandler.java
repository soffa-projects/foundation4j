package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.config.OperationsMapping;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import dev.soffa.foundation.core.Dispatcher;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.message.Message;
import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.message.MessageHandler;
import dev.soffa.foundation.model.ResponseEntity;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.security.PlatformAuthManager;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
@AllArgsConstructor
public class DefaultMessageHandler implements MessageHandler {

    private static final Logger LOG = Logger.get(DefaultMessageHandler.class);
    private final OperationsMapping mapping;
    private final PlatformAuthManager authManager;
    private final ApplicationContext context;
    private final AtomicReference<Dispatcher> dispatcher = new AtomicReference<>();

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public Optional<Object> handle(@NonNull Message message) {

        if (dispatcher.get() == null) {
            dispatcher.set(this.context.getBean(Dispatcher.class));
        }

        final Context context = message.getContext();
        ContextHolder.set(context);
        TenantHolder.set(context.getTenantId());
        Object operation = mapping.getInternal().get(message.getOperation());
        if (operation == null) {
            LOG.debug("Message %s skipped, no local handler registered", message.getOperation());
            return Optional.empty();
        }

        if (authManager != null && context.hasAuthorization()) {
            authManager.handle(context);
        }

        LOG.debug("New message received with operation %s#%s", message.getOperation(), message.getId());

        if (!(operation instanceof Operation)) {
            throw new TechnicalException("Unsupported operation type: " + operation.getClass().getName());
        }

        Class<?> inputType = mapping.getInputTypes().get(message.getOperation());
        if (inputType == null) {
            throw new TechnicalException("Unable to find input type for operation " + message.getOperation());
        }

        final AtomicReference<Object> payload = new AtomicReference<>();

        if (message.getPayload() != null) {
            if (TextUtil.isNotEmpty(message.getPayloadType())) {
                try {
                    LOG.debug("Deserializing message content into %s", message.getPayloadType());
                    payload.set(MessageFactory.getPayload(message, Class.forName(message.getPayloadType())));
                } catch (ClassNotFoundException e) {
                    LOG.error("Unable to deserialize message into %s", message.getPayloadType());
                }
            }
            if (payload.get() == null) {
                LOG.debug("Deserializing message content into %s", inputType.getName());
                payload.set(MessageFactory.getPayload(message, inputType));
            }
        }

        if (payload.get() == null) {
            LOG.debug("Invoking operation %s with empty payload", operation.getClass().getSimpleName());
        } else {
            LOG.debug("Invoking operation %s with payload of type %s", operation.getClass().getSimpleName(), payload.get().getClass().getSimpleName());
        }
        TenantHolder.set(context.getTenantId());
        //noinspection unchecked
        Object result = dispatcher.get().invoke((Operation<Object, Object>) operation, payload.get(), context);
        if (result == null) {
            return Optional.empty();
        }
        if (result instanceof ResponseEntity) {
            //TODO: handle status ?
            result = ((ResponseEntity<?>) result).getData();
        }
        return Optional.of(result);
    }
}
