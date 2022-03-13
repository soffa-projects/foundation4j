package dev.soffa.foundation.annotations;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Repository {

    String collection();
    String fixedTenant() default "";
}