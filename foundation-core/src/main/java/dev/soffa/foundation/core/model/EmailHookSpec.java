package dev.soffa.foundation.core.model;

import lombok.Data;

@Data
public class EmailHookSpec {

    public static final String TYPE = "email";

    private String to;
    private String subject;
    private String body;



}
