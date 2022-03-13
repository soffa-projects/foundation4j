package dev.soffa.foundation.data.migrations.model;

import lombok.Data;

import java.util.List;

@Data
public class ChangeDef {

    private String op;
    private String name;
    private List<ColumnDef> columns;
}
