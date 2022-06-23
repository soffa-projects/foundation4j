package dev.soffa.foundation.hooks.model;

import lombok.Data;

import java.util.Map;

@Data
public class HookItem {

    private String name;
    private String type;
    private Map<String, Object> spec;

}
