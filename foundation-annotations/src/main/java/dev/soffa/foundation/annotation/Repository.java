package dev.soffa.foundation.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Repository {

    String collection();
    String fixedTenant() default "";
}
