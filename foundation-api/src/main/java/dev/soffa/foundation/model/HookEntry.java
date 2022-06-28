package dev.soffa.foundation.model;

import lombok.Value;

import java.util.Map;

@Value
public class HookEntry {
    String id;
    String subject;
    Map<String, Object> data;
}
