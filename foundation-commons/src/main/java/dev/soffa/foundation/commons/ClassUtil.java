package dev.soffa.foundation.commons;


import com.google.gson.internal.Primitives;
import dev.soffa.foundation.error.TechnicalException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class ClassUtil {

    public static List<Field> getDeclaredFields(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields()).filter(field -> {
            // EL
            return !Modifier.isTransient(field.getModifiers());
        }).collect(Collectors.toList());
    }


    public static Set<Class<?>> findInterfacesAnnotatedWith(String basePackage, Class<? extends Annotation> annotationClass) {
        return findInterfacesAnnotatedWith(basePackage, annotationClass, null);
    }

    public static Set<Class<?>> findInterfacesAnnotatedWith(String basePackage, Class<? extends Annotation> annotationClass,
                                                            Class<?> parentInterface) {
        Set<Class<?>> resources = new HashSet<>();
        try (ScanResult scanResult =
                 new ClassGraph()
                     .enableClassInfo()
                     .enableAnnotationInfo()
                     .acceptPackages(basePackage)
                     .addClassLoader(Thread.currentThread().getContextClassLoader())
                     .scan()) {
            for (ClassInfo ci : scanResult.getClassesWithAnnotation(annotationClass)) {
                if (ci.isInterface()) {
                    Class<?> clazz = ci.loadClass();
                    if (parentInterface == null || parentInterface.isAssignableFrom(clazz)) {
                        resources.add(clazz);
                    }
                }
            }
        }
        return resources;
    }

    @NonNull
    public static Class<?>[] lookupGeneric(@NonNull Class<?> type, @NonNull Class<?> genericClass) {
        List<Type> parents = new ArrayList<>();
        if (type.getSuperclass() != null) {
            parents.add(type.getSuperclass());
        }
        parents.addAll(Arrays.asList(type.getInterfaces()));

        if (parents.isEmpty() || parents.size() == 1 && type.getSuperclass() == Object.class) {
            return null;
        }

        for (Type candidate : parents) {
            if (candidate == genericClass) {
                for (Type gi : type.getGenericInterfaces()) {
                    if (TypeUtils.isAssignable(gi, genericClass)) {
                        return Arrays.stream(((ParameterizedType) gi).getActualTypeArguments()).map(t -> {
                            //EL
                            return (Class<?>) t;
                        }).toArray(Class<?>[]::new);
                    }
                }
            }
        }

        for (Type parent : parents) {
            Class<?>[] match = lookupGeneric((Class<?>) parent, genericClass);
            if (match != null && match.length > 0) {
                return match;
            }
        }
        return new Class<?>[]{};
    }


    public static boolean isCollection(Class<?> type) {
        return Collection.class.isAssignableFrom(type) || type.isArray();
    }

    public static boolean isGenericCollection(Class<?> type) {
        if (type == null) return false;
        return isCollection(type) && type.getTypeParameters().length > 0;
    }

    public static Class<?> getGenericType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) type;
            return (Class<?>) ptype.getActualTypeArguments()[0];
        } else {
            throw new TechnicalException("getGenericType failed");
        }
    }

    @SneakyThrows
    public static <E> E newInstance(Class<E> clazz) {
        return clazz.getConstructor().newInstance();
    }

    public static boolean isBaseType(@NonNull Type type) {
        if (Primitives.isPrimitive(type) || Primitives.isWrapperType(type)) {
            return true;
        }
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isEnum()) {
                return true;
            }
        }
        return type.getTypeName().startsWith("java.") || type.getTypeName().startsWith("javax.");
    }
}
