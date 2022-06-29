package dev.soffa.foundation.model;

import lombok.Data;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Data
public class DataPoint {

    private final String metric;
    private Date time;
    private final Map<String, String> tags = new HashMap<>();
    private final Map<String, Object> fields = new HashMap<>();

    public DataPoint(String metric) {
        this.time = Date.from(Instant.now());
        this.metric = metric;
    }

    public static DataPoint metric(String name) {
        return new DataPoint(name);
    }


    public DataPoint addTags(Map<String, String> tags) {
        this.tags.putAll(tags);
        return this;
    }

    public DataPoint addFields(Map<String, Object> fields) {
        this.fields.putAll(fields);
        return this;
    }


    public DataPoint addTag(String name, String value) {
        tags.put(name, value);
        return this;
    }

    public DataPoint addField(String name, long value) {
        fields.put(name, value);
        return this;
    }

    public DataPoint addField(String name, double value) {
        fields.put(name, value);
        return this;
    }

    public DataPoint addField(String name, boolean value) {
        fields.put(name, value);
        return this;
    }

    public DataPoint addField(String name, CharSequence value) {
        fields.put(name, value);
        return this;
    }

    public DataPoint time(Date value) {
        this.time = value;
        return this;
    }

}
