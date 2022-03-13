package dev.soffa.foundation;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootApplication
public @interface Application {
}
