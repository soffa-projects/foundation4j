package dev.soffa.foundation.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Sentry {

    String label();

    boolean errorPropagation() default true;
}
