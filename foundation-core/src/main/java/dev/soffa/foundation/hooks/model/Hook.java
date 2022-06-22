package dev.soffa.foundation.hooks.model;

import lombok.Data;

import java.util.List;

@Data
public class Hook {

    public static final String EMAIL = "email";
    public static final String NOTIFICATION = "notification";

    private String operation;
    private String kind;
    private List<HookItem> post;

}
