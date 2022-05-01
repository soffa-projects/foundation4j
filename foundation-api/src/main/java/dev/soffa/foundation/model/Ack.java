package dev.soffa.foundation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class Ack {

    public static final String OK_VALUE = "OK";
    public static final String KO_VALUE = "KO";
    public static final Ack OK = new Ack(OK_VALUE);
    public static final Ack KO = new Ack(KO_VALUE);

    private String status;
    private String message;
    private Map<String, Object> metadata;

    public Ack(String status) {
        this.status = status;
    }

    public Ack(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public static Ack ko(String message) {
        return new Ack(KO_VALUE, message);
    }

    public static Ack ok(String message) {
        return new Ack(OK_VALUE, message);
    }

    @JsonIgnore
    public boolean isOK() {
        return OK_VALUE.equalsIgnoreCase(status);
    }

    @JsonIgnore
    public boolean isKO() {
        return KO_VALUE.equalsIgnoreCase(status);
    }

}
