package dev.soffa.foundation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class Foundation {

    private Foundation() {
    }

    public static void run(Class<?> applicationClass, String... args) {
        SpringApplication springApplication = new SpringApplication(applicationClass);
        // String[] profiles= new String[]{};
        // springApplication.setAdditionalProfiles(profiles);
        springApplication.setWebApplicationType(WebApplicationType.SERVLET);
        springApplication.run(args);
    }
}
