package dev.soffa.foundation.data.app.model;

import dev.soffa.foundation.annotation.Store;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Store("readings")
@AllArgsConstructor
public class Metric {

    private long ts;
    private long sensorId;
    private double temp;
}
