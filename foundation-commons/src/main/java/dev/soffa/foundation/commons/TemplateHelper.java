package dev.soffa.foundation.commons;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import dev.soffa.foundation.error.TechnicalException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public final class TemplateHelper {

    private static final PebbleEngine PEBBLE = new PebbleEngine.Builder().build();

    private TemplateHelper() {
    }

    public static String render(InputStream template, Map<String, Object> context) {
        String content = IOUtil.toString(template).orElseThrow(() -> new TechnicalException("Error while opening template"));
        return render(content, context);
    }
    public static String render(PebbleEngine engine, InputStream template, Map<String, Object> context) {
        String content = IOUtil.toString(template).orElseThrow(() -> new TechnicalException("Error while opening template"));
        return render(engine, content, context);
    }

    public static String render(File template, Map<String, Object> context) {
        try (InputStream is = Files.newInputStream(Paths.get(template.toURI()))) {
            return render(is, context);
        } catch (IOException e) {
            throw new TechnicalException("Error while rendering template", e);
        }
    }

    public static String render(String template, Map<String, Object> context) {
        return render(PEBBLE, template, context);
    }

    public static String render(PebbleEngine engine, String template, Map<String, Object> context) {
        PebbleTemplate compiledTemplate = engine.getLiteralTemplate(template);
        Writer writer = new StringWriter();
        try {
            compiledTemplate.evaluate(writer, context);
            return writer.toString();
        } catch (IOException e) {
            throw new TechnicalException("Error while rendering template", e);
        }
    }

}

