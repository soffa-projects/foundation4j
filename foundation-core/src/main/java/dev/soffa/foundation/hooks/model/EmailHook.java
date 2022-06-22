package dev.soffa.foundation.hooks.model;

import lombok.Data;

@Data
public class EmailHook {

    public static final String TYPE = "email";

    private String to;
    private String subject;
    private String body;



}
