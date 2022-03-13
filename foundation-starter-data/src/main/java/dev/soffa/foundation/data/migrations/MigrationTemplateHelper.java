package dev.soffa.foundation.data.migrations;

import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.commons.TemplateHelper;

import java.io.InputStream;
import java.util.Map;

public final class MigrationTemplateHelper {

    private MigrationTemplateHelper() {}

    public static String transform(InputStream context) {
        return transform(Mappers.YAML.deserializeMap(context));
    }

    public static String transform(Map<String,Object> context) {

        return TemplateHelper.render(
            MigrationTemplateHelper.class.getResourceAsStream("/templates/liquibase.xml.peb"),
            context
        );
    }
}
