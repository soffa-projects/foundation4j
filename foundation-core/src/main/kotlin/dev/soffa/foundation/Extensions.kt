package dev.soffa.foundation

import dev.soffa.foundation.data.RepositoryFactory
import dev.soffa.foundation.model.TenantId
import kotlin.reflect.KClass

inline fun <reified T> RepositoryFactory.get(): T {
    return this.get(T::class.java)
}

inline fun <reified T> RepositoryFactory.get(tenant: TenantId): T {
    return this.get(T::class.java, tenant)
}

inline fun <reified T> RepositoryFactory.get(tenant: String): T {
    return this.get(T::class.java, TenantId.of(tenant))
}

fun <T : Any> RepositoryFactory.get(clazz: KClass<T>, tenant: String): T {
    return this.get(clazz.java, TenantId.of(tenant))
}

fun <T : Any> RepositoryFactory.get(clazz: KClass<T>, tenant: TenantId): T {
    return this.get(clazz.java, tenant)
}

fun <T : Any> RepositoryFactory.get(clazz: KClass<T>): T {
    return this.get(clazz.java)
}
