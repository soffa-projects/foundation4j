package dev.soffa.foundation.config;

import dev.soffa.foundation.annotation.Handle;
import dev.soffa.foundation.commons.ClassUtil;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.context.DefaultOperationContext;
import dev.soffa.foundation.core.Command;
import dev.soffa.foundation.core.EventHandler;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.core.Query;
import dev.soffa.foundation.error.ConfigurationException;
import dev.soffa.foundation.error.TechnicalException;
import lombok.Getter;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Type;
import java.util.*;

@Getter
public class OperationsMapping {

    private final Set<Operation<?, ?>> registry;
    private final Map<String, Object> internal = new HashMap<>();
    private final Map<String, String> operationNames = new HashMap<>();
    private final Map<String, Type> inputTypes = new HashMap<>();

    public OperationsMapping(Set<Operation<?, ?>> registry) {
        this.registry = registry;
        register(registry);
    }

    public boolean isEmpty() {
        return registry.isEmpty();
    }

    public Optional<Operation<?, ?>> lookup(String name) {
        return Optional.ofNullable((Operation<?, ?>) internal.get(name));
    }

    @SuppressWarnings("unchecked")
    public <I, O, T extends Operation<I, O>> T invoke(String name) {
        Operation<I,O> op = require(name);
        return (T) op.handle(null, DefaultOperationContext.create(op.getClass()));
    }

    public void send(String name) {
        Operation<?,?> op = require(name);
        op.handle(null, DefaultOperationContext.create(op.getClass()));
    }

    @SuppressWarnings("unchecked")
    public <I, O, T extends Operation<I, O>> T require(String name) {
        return (T) lookup(name).orElseThrow(() -> new TechnicalException("Operation not found: " + name));
    }


    public <I, O, T extends Operation<I, O>> T require(Class<T> operationClass) {
        return require(resolveClass(operationClass).getName());
    }

    @SneakyThrows
    public static Class<?> resolveClass(Object op) {
        Class<?> targetClass = op instanceof Class<?> ? (Class<?>) op : op.getClass();
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

                    register(targetClass, operation, bindingName);
                    if (intf != Operation.class && intf != EventHandler.class && intf != Command.class && intf != Query.class) {
                        register(intf, operation, bindingName);
                        operationNames.put(targetClass.getName(), intf.getSimpleName());
                        operationNames.put(targetClass.getSimpleName(), intf.getSimpleName());
                    }
                    break;
                }
            }
        }
    }

    private void register(Class<?> target, Object operation, String bindingName) {
        internal.put(target.getSimpleName(), operation);
        internal.put(target.getName(), operation);

        Class<?> resolvedClass = resolveClass(operation);
        Type[] signature = ClassUtil.lookupGeneric(resolvedClass, Operation.class);

        if (signature==null || signature.length == 0) {
            throw new ConfigurationException("%s is not a valid operation definition", operation.getClass().getName());
        }
        Type inputType = signature[0];
        if (inputType == Object.class) {
            throw new ConfigurationException("Operation %s has an invalid input type (Object)", operation.getClass().getName());
        }
        inputTypes.put(target.getSimpleName(), inputType);
        inputTypes.put(target.getName(), inputType);

        if (bindingName != null) {
            inputTypes.put(bindingName, inputType);
        }

    }


    public String getOperationId(@NonNull Object operation) {
        String className = resolveClass(operation).getSimpleName();
        String operationId;
        if (operationNames.containsKey(className)) {
            operationId = operationNames.get(className);
        } else {
            operationId = operation.getClass().getSimpleName();
        }
        return operationId;
    }
}
