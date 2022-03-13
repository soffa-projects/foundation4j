package dev.soffa.foundation.config;

import dev.soffa.foundation.annotations.Handle;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.errors.TechnicalException;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.util.*;

@Getter
public class OperationsMapping {

    private final Set<Operation<?, ?>> registry;
    private final Map<String, Object> internal = new HashMap<>();
    private final Map<String, Class<?>> inputTypes = new HashMap<>();

    public OperationsMapping(Set<Operation<?, ?>> registry) {
        this.registry = registry;
        register(registry);
    }

    public boolean isEmpty() {
        return registry.isEmpty();
    }

    public Optional<Operation<?,?>> lookup(String name) {
        return Optional.ofNullable((Operation<?,?>)internal.get(name));
    }

    @SneakyThrows
    private Class<?> resolveClass(Object op) {
        Class<?> targetClass = op.getClass();
        if (AopUtils.isAopProxy(op) && op instanceof Advised) {
            Object target = ((Advised) op).getTargetSource().getTarget();
            targetClass = Objects.requireNonNull(target).getClass();
        }
        return targetClass;
    }

    private Optional<String> registerAnyBinding(Class<?> targetClass, Object operation) {
        String bindingName = null;
        Handle binding = targetClass.getAnnotation(Handle.class);
        if (binding != null) {
            if (TextUtil.isEmpty(binding.value())) {
                throw new TechnicalException("@BindOperation on a type should have the property name set.");
            }
            bindingName = binding.value();
            internal.put(binding.value(), operation);
        }
        return Optional.ofNullable(bindingName);
    }

    @SneakyThrows
    private void register(Set<?> operations) {
        for (Object operation : operations) {
            Class<?> targetClass = resolveClass(operation);
            String bindingName = registerAnyBinding(targetClass, operation).orElse(null);

            for (Class<?> intf : targetClass.getInterfaces()) {
                if (Operation.class.isAssignableFrom(intf)) {
                    Method method = Arrays.stream(operation.getClass().getMethods())
                        .filter(m -> "handle".equals(m.getName()) && 2 == m.getParameterCount() && m.getParameterTypes()[1] == Context.class)
                        .findFirst().orElseThrow(() -> new TechnicalException("Invalid operation definition"));

                    if (intf != Operation.class) {
                        register(intf, operation, method, bindingName);
                    } else {
                        register(targetClass, operation, method, bindingName);
                    }
                    break;
                }
            }
        }
    }

    private void register(Class<?> target, Object operation, Method method, String bindingName) {
        internal.put(target.getSimpleName(), operation);
        internal.put(target.getName(), operation);


        Class<?> inputType = method.getParameterTypes()[0];
        inputTypes.put(target.getSimpleName(), inputType);
        inputTypes.put(target.getName(), inputType);

        if (bindingName != null) {
            inputTypes.put(bindingName, inputType);
        }

    }


}
