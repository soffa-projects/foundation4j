package dev.soffa.foundation.config;

import dev.soffa.foundation.commons.CollectionUtil;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.data.DbConfig;
import dev.soffa.foundation.openapi.OpenAPIDesc;
import dev.soffa.foundation.security.SecurityConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AppConfig {

    private boolean sysLogs = true;
    private String name;
    private String pkg;
    private String version;
    private DbConfig db;
    private OpenAPIDesc openapi;
    private SecurityConfig security;

    private boolean configured;

    public AppConfig(String name) {
        this.name = name;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasDataSources() {
        return db != null && CollectionUtil.isNotEmpty(db.getDatasources());
    }

    public void configure() {
        if (configured) {
            return;
        }
        if (db != null && TextUtil.isNotEmpty(db.getTablesPrefix())) {
            String value = TextUtil.trimToEmpty(db.getTablesPrefix())
                .replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("_+$", "_").trim();

            if (!value.endsWith("_")) {
                value += "_";
            }
            db.setTablesPrefix(value);
        }
        Context.setServiceName(name);
        configured = true;
    }

}
