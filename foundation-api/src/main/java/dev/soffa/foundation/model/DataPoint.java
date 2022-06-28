package dev.soffa.foundation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Map;


@Data
@AllArgsConstructor
@Builder
public class DataPoint {
    private String metric;
    private Date time;
    private Map<String, String> tags;
    private Map<String, Object> fields;
}
