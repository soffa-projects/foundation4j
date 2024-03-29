package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Sentry;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.config.OperationsMapping;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import dev.soffa.foundation.core.Dispatcher;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.error.NotImplementedException;
import dev.soffa.foundation.message.DispatchMessageHandler;
import dev.soffa.foundation.message.Message;
import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.model.ResponseEntity;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.security.PlatformAuthManager;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
@AllArgsConstructor
public class OperationDispatchMessageHandler implements DispatchMessageHandler {

    private static final Logger LOG = Logger.get(OperationDispatchMessageHandler.class);
    //private final OperationsMapping mapping;
    private final PlatformAuthManager authManager;
    private final ApplicationContext context;
    private final AtomicReference<Dispatcher> dispatcher = new AtomicReference<>();
    private final AtomicReference<OperationsMapping> operationsMapping = new AtomicReference<>(null);

    private OperationsMapping getOperations() {
        if (operationsMapping.get() == null) {
            operationsMapping.set(context.getBean(OperationsMapping.class));
        }
        return operationsMapping.get();
    }

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public Optional<Object> handle(Message message) {

        if (message == null) {
            Logger.platform.error("Invalid pubsubs message received");
            return Optional.empty();
        }

        if (dispatcher.get() == null) {
            try {
                dispatcher.set(this.context.getBean(Dispatcher.class));
            } catch (Exception e) {
                Logger.platform.error("Unable to locate Dispatcher bean in current context");
                return Optional.empty();
            }
        }


        Object operation = getOperations().getInternal().get(message.getOperation());
        if (operation == null) {
            LOG.debug("Message %s skipped, no local handler registered", message.getOperation());
            return Optional.empty();
        }

        final Context context = message.getContext();
        ContextHolder.set(context);
        TenantHolder.set(context.getTenantId());

        if (authManager != null && context.hasAuthorization()) {
            authManager.handle(context);
        }

        LOG.info("[pubsub] New message received with operation %s#%s", message.getOperation(), message.getId());

        if (!(operation instanceof Operation)) {
            Logger.platform.error("[pubsub] unsupported operation type: %s", operation.getClass().getName());
            return Optional.empty();
        }

        Type inputType = getOperations().getInputTypes().get(message.getOperation());
        if (inputType == null) {
            Logger.platform.error("[pubsub] Unable to find input type for operation %s", message.getOperation());
            return Optional.empty();
        }

        final AtomicReference<Object> payload = new AtomicReference<>();

        if (message.getPayload() != null) {
            if (TextUtil.isNotEmpty(message.getPayloadType())) {
                try {
                    LOG.debug("[pubsub] Deserializing message content into %s", message.getPayloadType());
                    payload.set(MessageFactory.getPayload(message, Class.forName(message.getPayloadType())));
                } catch (ClassNotFoundException e) {
                    LOG.error("[pubsub] Unable to deserialize message into %s", message.getPayloadType());
                }
            }
            if (payload.get() == null) {
                LOG.debug("Deserializing message content into %s", inputType.getTypeName());
                if (inputType instanceof Class<?>) {
                    payload.set(MessageFactory.getPayload(message, (Class<?>) inputType));
                } else {
                    throw new NotImplementedException("TYPE_NOT_IMEPLEMENTED: Please contact the developer");
                }
            }
        }

        if (payload.get() == null) {
            LOG.debug("Invoking operation %s with empty payload", operation.getClass().getSimpleName());
        } else {
            LOG.debug("Invoking operation %s with payload of type %s", operation.getClass().getSimpleName(), payload.get().getClass().getSimpleName());
        }

        return Sentry.get().watch("pubsub event: " + message.getOperation(), () -> {
            @SuppressWarnings("unchecked")
            Object result = dispatcher.get().invoke((Operation<Object, Object>) operation, payload.get(), context);

            if (result == null) {
                return Optional.empty();
            }
            if (result instanceof ResponseEntity) {
                //TODO: handle status ?
                result = ((ResponseEntity<?>) result).getData();
            }
            return Optional.of(result);
        });
    }
}
