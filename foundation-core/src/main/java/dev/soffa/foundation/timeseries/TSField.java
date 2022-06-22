package dev.soffa.foundation.timeseries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TSField {

    private String name;
    private TSFieldType type;
    private boolean indexed;

    public TSField(String name, TSFieldType type) {
        this.name = name;
        this.type = type;
    }
}
