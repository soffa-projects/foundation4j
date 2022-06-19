package dev.soffa.foundation.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class TemplateMessage {

    private String template;
    private Map<String, Object> values = new HashMap<>();

    public TemplateMessage(String template) {
        this.template = template;
    }

    public TemplateMessage(String template, Map<String, Object> values) {
        this.template = template;
        this.values = values;
    }
}
