package dev.soffa.foundation.starter.test;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.context.Context;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContextSerializationTest {


    @Test
    public void testSerialization() {
        Context ctx = Context.create("t1");
        ctx.setApplicationName("application");
        String out1 = Mappers.JSON_DEFAULT.serialize(ctx);
        assertNotNull(out1);
        assertTrue(out1.contains("application_name"));

        String out2 = Mappers.JSON_DEFAULT.serialize(ImmutableMap.of("context", ctx));
        assertNotNull(out2);
        assertTrue(out2.contains("application_name"));
    }
}
