package dev.soffa.foundation.starter.test.app.model;

import lombok.Data;

import java.util.List;

@Data
public class ConfigModel {

    private boolean enabled;
    private String interval;
    private List<String> options;

}
