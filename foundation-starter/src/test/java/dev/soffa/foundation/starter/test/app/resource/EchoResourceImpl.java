package dev.soffa.foundation.starter.test.app.resource;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Dispatcher;
import dev.soffa.foundation.core.Operation;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class EchoResourceImpl implements EchoResource {

    private Dispatcher dispatcher;

    @Override
    public <I, O, T extends Operation<I, O>> O invoke(Class<T> operationClass, I input, @NonNull Context ctx) {
        return dispatcher.dispatch(operationClass, input);
    }

    @Override
    public <I, O, T extends Operation<I, O>> O invoke(Class<T> operationClass, I input) {
        return dispatcher.dispatch(operationClass, input);
    }
}
