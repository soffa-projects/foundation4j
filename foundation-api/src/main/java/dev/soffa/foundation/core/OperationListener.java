package dev.soffa.foundation.core;

import dev.soffa.foundation.context.Context;

public interface OperationListener<E> {

    void process(Context context, E result);

}
