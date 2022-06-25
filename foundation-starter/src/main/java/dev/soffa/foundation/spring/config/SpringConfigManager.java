package dev.soffa.foundation.spring.config;

import dev.soffa.foundation.commons.ClassUtil;
import dev.soffa.foundation.config.ConfigManager;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SpringConfigManager implements ConfigManager {

    private final Environment env;

    @SneakyThrows
    @Override
    public <T> T bind(String prefix, Class<T> kind) {
        Binder binder = Binder.get(env);
        Bindable<T> target = Bindable.ofInstance(ClassUtil.newInstance(kind));
        return binder.bind(prefix, target).get();
    }

    @Override
    public String require(String name) {
        return env.getRequiredProperty(name);
    }

}
