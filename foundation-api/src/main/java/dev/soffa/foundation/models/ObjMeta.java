package dev.soffa.foundation.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

@Getter
public class ObjMeta {

    @JsonProperty("object")
    private transient String object;

    private Map<String, Object> metadata;

    private ObjMeta() {
    }

    public ObjMeta(String object) {
        this.object = object;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
