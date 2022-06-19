package dev.soffa.foundation.data.migrations.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class MigrationDef {

    private String kind;
    @JsonProperty("api_version")
    private String apiVersion;
    private String id;
    @JsonProperty("change_sets")
    private Map<String, ChangeSetDef> changeSets;
}
