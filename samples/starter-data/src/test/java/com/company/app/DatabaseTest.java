package com.company.app;

import dev.soffa.foundation.data.DB;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("test")
public class DatabaseTest {

    @Autowired
    private DB db;

    @Test
    public void testTenantsProvisioning() {
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).until(() -> db.tenantExists("tx001"));
    }

}
