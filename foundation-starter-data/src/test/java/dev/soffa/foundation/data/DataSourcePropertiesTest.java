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


        /*
        DataSourceProperties props2 = DataSourceProperties.create("app", "tenant", "pg://postgres:postgres@localhost/bantu_test?schema=public");

        props2.setSchema("tenant1");
        DataSource ds1 = DBHelper.createDataSource("tenant1", props2);

        props2.setSchema("tenant2");
        DataSource ds2 = DBHelper.createDataSource("tenant2", props2);

        props2.setSchema("tenant3");
        DataSource ds3 = DBHelper.createDataSource("tenant3", props2);

        Jdbi.create(ds1).withHandle(handle -> handle.execute("select 1"));
        Jdbi.create(ds2).withHandle(handle -> handle.execute("select 1"));
        Jdbi.create(ds3).withHandle(handle -> handle.execute("select 1"));
        */
    }
}
