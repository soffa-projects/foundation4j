package dev.soffa.foundation.core;

import dev.soffa.foundation.resource.Resource;

public interface BaseDispatcher extends Resource {
    <I, O, T extends Operation<I, O>> O dispatch(Class<T> operationClass, I input);
}
