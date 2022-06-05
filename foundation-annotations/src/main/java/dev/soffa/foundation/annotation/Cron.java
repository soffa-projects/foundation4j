package dev.soffa.foundation.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Cron {

    String value();

    String EVERY_1_MINUTE = "* * * * *";
    String EVERY_5_SECONDS = "*/5 * * * * *";
    String EVERY_10_SECONDS = "*/10 * * * * *";
    String EVERY_30_SECONDS = "*/30 * * * * *";
    String EVERY_1_HOUR = "0 * * * * *";
}
