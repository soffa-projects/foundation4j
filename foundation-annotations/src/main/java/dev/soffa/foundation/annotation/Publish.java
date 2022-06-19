package dev.soffa.foundation.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Publish {

    String SELF_TARGET_1 = "self";
    String SELF_TARGET_2 = "@";
    String BROADCAST_TARGET = "*";

    String event();

    String target() default SELF_TARGET_2;

}
