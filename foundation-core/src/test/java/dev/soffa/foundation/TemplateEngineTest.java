package dev.soffa.foundation;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.TemplateHelper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemplateEngineTest {

    @Test
    public void testDefaultEngine() {
        String output = TemplateHelper.render("Hello {{ name }}", ImmutableMap.of("name", "SOFFA"));
        assertEquals(output, "Hello SOFFA");
    }

    @Test
    public void testDefaultEngineInputstream() {
        InputStream source = new ByteArrayInputStream("Hello {{ name }}".getBytes(StandardCharsets.UTF_8));
        String output = TemplateHelper.render(source, ImmutableMap.of("name", "SOFFA"));
        assertEquals(output, "Hello SOFFA");
    }

}
