package dev.soffa.foundation.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor
public class Request {

    private String url;
    private String body;
    private Map<String, String> params;
    private Map<String, String> headers;

    public Optional<String> header(String name) {
        return Optional.ofNullable(headers.get(name));
    }

    public Optional<String> param(String name) {
        return Optional.ofNullable(params.get(name));
    }

}
