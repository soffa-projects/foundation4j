package dev.soffa.foundation.spring.resource;

import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

// @RestController
// @AllArgsConstructor
// @Tag(name = "Health", description = "Service healthcheck")
public class PlatformHealthCheck {

    private HealthEndpoint health;
    private Environment env;

    // @GetMapping("/health")
    public HealthComponent health() {
        boolean isProduction = env.acceptsProfiles(Profiles.of("prod", "production"));
        if (isProduction) {
            return health.health();
        }else {
            return health.health();
        }
    }
}
