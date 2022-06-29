package dev.soffa.foundation.core.model;

import lombok.Data;

import java.util.Map;

@Data
public class NotificationHook {

    private String message;
    private Map<String,String> context;

}
