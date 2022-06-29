package dev.soffa.foundation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HookEntry {
    private String id;
    private String subject;
    private Map<String, Object> data;
}
