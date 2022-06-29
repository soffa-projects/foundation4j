package dev.soffa.foundation.commons;


import com.google.gson.internal.Primitives;
import dev.soffa.foundation.error.TechnicalException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.SneakyThrows;
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


    public static Type[] lookupGeneric(Class<?> type, @NonNull Class<?> genericClass) {
        if (type == null || type == Object.class) {
            return null;
        }

        Type[] result = null;

        final List<Type> candidates = new ArrayList<>();
        Optional.of(type.getGenericInterfaces()).ifPresent(types -> candidates.addAll(Arrays.asList(types)));
        Optional.ofNullable(type.getGenericSuperclass()).ifPresent(candidates::add);

        for (Type candidate : candidates) {
            if (candidate instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType) candidate;
                result = lookupGeneric(ptype, genericClass);
            } else if (candidate instanceof Class<?>) {
                result = lookupGeneric((Class<?>)candidate, genericClass);
            } else {
                throw new TechnicalException("Unsupported type: " + candidate);
            }
            if (result != null) {
                break;
            }
        }

        return result;
    }

    public static Type[] lookupGeneric(ParameterizedType candidate, @NonNull Type genericClass) {
        boolean matches = genericClass == candidate.getRawType();
        if (matches) {
            return candidate.getActualTypeArguments();
        }
        if (candidate.getRawType() instanceof Class<?>) {
            Class<?> rawType = (Class<?>) candidate.getRawType();
            if (((Class<?>)genericClass).isAssignableFrom(rawType)) {
                return new Type[]{candidate};
            }
        }
        return null;
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
