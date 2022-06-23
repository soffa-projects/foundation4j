package dev.soffa.foundation.activity;

import lombok.Data;

@Data
public class Activity {

    private String event;
    private String data;

    public Activity(String event, String data) {
        this.event = event;
        this.data = data;
    }
}
