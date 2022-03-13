package dev.soffa.foundation.data.migrations.model;

import lombok.Data;

import java.util.List;

@Data
public class ChangeSetDef {

    private String author;
    private List<ChangeDef> changes;
}
