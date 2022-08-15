package dev.soffa.foundation.timeseries;

import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.error.TechnicalException;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
public class TSTable {

    private final Map<String, TSField> fields = new LinkedHashMap<>();
    private String name;
    private String timestampField;

    public TSTable(String name) {
        this.name = name;
    }

    public TSTable field(String name) {
        return field(name, TSFieldType.STRING);
    }

    public TSTable field(String name, TSFieldType type) {
        return field(name, type, false);
    }

    public TSTable field(String name, TSFieldType type, boolean index) {
        fields.put(name, new TSField(name, type, index));
        return this;
    }

    public TSTable timestamp(String fieldName) {
        if (TextUtil.isNotEmpty(timestampField)) {
            throw new TechnicalException("A timestamp field is already defined: " + timestampField);
        }
        timestampField = fieldName;
        return field(fieldName, TSFieldType.TIMESTAMP);
    }
}
