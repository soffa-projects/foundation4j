package dev.soffa.foundation.commons.http;

import okhttp3.Headers;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HttpHeaders {

    public static final String CONTENT_TYPE = "content-type";
    private final Map<String, List<String>> headers = new HashMap<>();

    public HttpHeaders(Map<String, List<String>> headers) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                this.headers.put(e.getKey().toLowerCase(), e.getValue());
            }
        }
    }

    public static HttpHeaders of(Headers h) {
        return new HttpHeaders(h.toMultimap());
    }

    public boolean containsKey(@NonNull String name) {
        return headers.containsKey(name.toLowerCase());
    }

    public Optional<String> first(@NonNull String name) {
        if (!containsKey(name)) {
            return Optional.empty();
        }
        return Optional.of(headers.get(name.toLowerCase()).get(0));
    }

    public boolean equals(@NonNull String name, @NonNull String value) {
        return value.equalsIgnoreCase(first(name).orElse(null));
    }

    public List<String> get(@NonNull String name) {
        return headers.get(name.toLowerCase());
    }

    public boolean contentTypeIs(String value) {
        return equals(CONTENT_TYPE, value);
    }


}
