package dev.soffa.foundation.core.model;

import lombok.Data;

import java.util.List;

@Data
public class HookSpec {

    public static final String EMAIL = "email";
    public static final String NOTIFICATION = "notification";

    private String operation;
    private String kind;
    private List<HookItemSpec> post;

}
