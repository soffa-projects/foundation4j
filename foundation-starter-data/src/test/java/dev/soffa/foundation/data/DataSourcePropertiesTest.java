package dev.soffa.foundation.data;

import dev.soffa.foundation.data.config.DataSourceProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DataSourcePropertiesTest {

    @Test
    public void testDataSourceProperties() {
        DataSourceProperties props = DataSourceProperties.create("app", "default", "h2://mem/default?schema=public&maxPoolSize=20");
        assertNotNull(props);
        assertNotNull(props.getProperties());
        assertEquals("public", props.property("schema"));
        assertEquals("20", props.property("maxPoolSize"));
        assertEquals("jdbc:h2:mem:default;MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS PUBLIC", props.getUrl());

    }
}
