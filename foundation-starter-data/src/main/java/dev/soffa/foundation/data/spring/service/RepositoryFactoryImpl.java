package dev.soffa.foundation.data.spring.service;

import dev.soffa.foundation.data.RepositoryFactory;
import dev.soffa.foundation.model.TenantId;
import org.springframework.stereotype.Component;

@Component
public class RepositoryFactoryImpl implements RepositoryFactory {

    @Override
    public <T> T get(Class<T> clazz, TenantId tenantId) {
        return null;
    }

}
