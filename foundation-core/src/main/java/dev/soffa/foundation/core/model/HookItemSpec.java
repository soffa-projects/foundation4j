package dev.soffa.foundation.core.model;

import lombok.Data;

import java.util.Map;

@Data
public class HookItemSpec {

    private String name;
    private String type;
    private Map<String, Object> spec;

}
