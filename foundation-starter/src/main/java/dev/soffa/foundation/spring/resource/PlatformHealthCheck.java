package dev.soffa.foundation.spring.resource;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Tag(name = "Health", description = "Service healthcheck")
public class PlatformHealthCheck {

    private HealthEndpoint health;

    @GetMapping("/health")
    public HealthComponent health() {
        return health.health();
    }
}
