package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.config.OperationsMapping;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import dev.soffa.foundation.context.ContextUtil;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.error.TechnicalException;
import dev.soffa.foundation.message.Message;
import dev.soffa.foundation.message.MessageFactory;
import dev.soffa.foundation.message.MessageHandler;
import dev.soffa.foundation.metric.MetricsRegistry;
import dev.soffa.foundation.model.ResponseEntity;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.security.PlatformAuthManager;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Component
@AllArgsConstructor
public class DefaultMessageHandler implements MessageHandler {

    private static final Logger LOG = Logger.get(DefaultMessageHandler.class);
    private final OperationsMapping mapping;
    private final MetricsRegistry metricsRegistry;
    private final PlatformAuthManager authManager;

    @Override
    public Optional<Object> handle(@NonNull Message message) {
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

        //noinspection Convert2Lambda
        return metricsRegistry.track(
            "app_operation_" + message.getOperation(),
            ContextUtil.tagify(context),
            new Supplier<Optional<Object>>() {
                @SneakyThrows
                @Override
                public Optional<Object> get() {
                    if (payload.get() == null) {
                        LOG.debug("Invoking operation %s with empty payload", operation.getClass().getSimpleName());
                    } else {
                        LOG.debug("Invoking operation %s with payload of type %s", operation.getClass().getSimpleName(), payload.get().getClass().getSimpleName());
                    }
                    TenantHolder.set(context.getTenantId());
                    //noinspection unchecked
                    @SuppressWarnings("unchecked")
                    Object result = ((Operation<Object, Object>) operation).handle(payload.get(), context);
                    if (result == null) {
                        return Optional.empty();
                    }
                    if (result instanceof ResponseEntity) {
                        //TODO: handle status ?
                        result = ((ResponseEntity<?>) result).getData();
                    }
                    return Optional.of(result);
                }
            });
    }


}
