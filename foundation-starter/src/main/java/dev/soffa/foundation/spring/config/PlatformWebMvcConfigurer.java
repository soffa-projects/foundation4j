package dev.soffa.foundation.spring.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
class PlatformWebMvcConfigurer implements WebMvcConfigurer {

    @Value("${app.health.root.enabled:false}")
    private boolean healthRootEnabled;

    @Override
    public void addViewControllers(@NotNull ViewControllerRegistry registry) {
        if (healthRootEnabled) {
            registry.addViewController("/").setViewName("forward:/health");
        }
        registry.addViewController("/actuator/health").setViewName("forward:/health");
        registry.addViewController("/metrics").setViewName("forward:/actuator/prometheus");
    }

/*
    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        handlers.add(new ResponseEntityInterceptor());
    }
    */
}
