package dev.soffa.foundation.core

import kotlin.reflect.KClass

fun <I, O, T : Operation<I, O>> BaseDispatcher.dispatch(clazz: KClass<T>, input: I): O {
    return this.dispatch(clazz.java, input)
}

