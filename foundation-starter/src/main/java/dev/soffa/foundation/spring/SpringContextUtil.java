package dev.soffa.foundation.spring;

import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Optional;

public class SpringContextUtil {

    public static <T> Optional<T> findBean(ApplicationContext context, Class<T> clazz) {
        Map<String,T> beans = context.getBeansOfType(clazz);
        if (beans.isEmpty()) {
            return Optional.empty();
        }
        if (beans.size()>1) {
            throw new IllegalStateException("More than one bean of type " + clazz + " found");
        }
        return Optional.of(beans.values().iterator().next());
    }
}
