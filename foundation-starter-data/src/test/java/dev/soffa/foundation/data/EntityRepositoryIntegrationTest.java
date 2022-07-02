package dev.soffa.foundation.data;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@ContextConfiguration(initializers = {EntityRepositoryIntegrationTest.Initializer.class})
@SpringBootTest(properties = {"app.test.scheduler=mocked", "app.name=docker-test"})
@EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true")
@Testcontainers
public class EntityRepositoryIntegrationTest extends EntityRepositoryTest{


    @SuppressWarnings("rawtypes")
    @Container
    public static PostgreSQLContainer container = new PostgreSQLContainer("postgres:11")
        .withDatabaseName("foundation")
        .withUsername("postgres")
        .withPassword("postgres");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            assertTrue(container.isRunning());
            String url = String.format("postgres://%s:%s@%s:%d/%s",
                container.getUsername(),
                container.getPassword(),
                container.getHost(),
                container.getMappedPort(5432),
                container.getDatabaseName()
            );
            String tenantsUrl = url + "?schema=__tenant__";
            TestPropertyValues.of(
                "app.test.scheduler=mocked",
                "app.db.datasources.__tenant__.url=" + tenantsUrl,
                "app.db.datasources.default.url=" + url
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }



}
