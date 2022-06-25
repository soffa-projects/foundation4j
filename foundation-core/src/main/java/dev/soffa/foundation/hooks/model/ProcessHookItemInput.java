package dev.soffa.foundation.hooks.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class ProcessHookItemInput {

    private String operation;
    private String name;
    private String type;
    private String spec;
    private String data;

}
