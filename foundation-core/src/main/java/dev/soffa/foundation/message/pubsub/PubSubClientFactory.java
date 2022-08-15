package dev.soffa.foundation.message.pubsub;

import dev.soffa.foundation.commons.ClassUtil;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.core.RemoteOperation;
import dev.soffa.foundation.error.NotImplementedException;
import dev.soffa.foundation.message.Message;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class PubSubClientFactory {

    public static final AtomicLong ASYNC_TIMEOUT_SECONDS = new AtomicLong(30);

    private PubSubClientFactory() {
    }

    @SuppressWarnings({"unchecked"})
    public static <I, O, T extends Operation<I, O>> RemoteOperation<I, O> of(PubSubClient client, @NotNull Class<T> operationClass, @NonNull String channel) {
        Type returnType = Objects.requireNonNull(ClassUtil.lookupGeneric(operationClass, Operation.class))[1];
        return (RemoteOperation<I, O>) java.lang.reflect.Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[]{RemoteOperation.class},
            (proxy, method, args) -> {
                if ("hashCode".equals(method.getName())) {
                    return operationClass.getName().hashCode();
                }
                if ("equals".equals(method.getName())) {
                    return method.equals(args[0]);
                }
                if ("invoke".equalsIgnoreCase(method.getName())) {
                    Context context;
                    Object input = null;
                    boolean hasNoInput = args.length == 1;
                    if (hasNoInput) {
                        context = (Context) args[0];
                    } else {
                        input = args[0];
                        context = (Context) args[1];
                    }
                    Message msg = new Message(operationClass.getSimpleName(), input, context);
                    if (returnType instanceof Class<?>) {
                        return client.request(channel, msg, (Class<T>) returnType).get(ASYNC_TIMEOUT_SECONDS.get(), TimeUnit.SECONDS);
                    } else {
                        throw new NotImplementedException("Return type is not a class (Type type not implemented yet, please contact the developer)");
                    }
                } else {
                    throw new NotImplementedException("Method not implemented yet, please contact the developer");
                }
            });
    }

}
