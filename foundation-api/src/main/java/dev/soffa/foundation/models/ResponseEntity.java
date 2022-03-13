package dev.soffa.foundation.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

@Getter
public final class ResponseEntity<T> {

    @JsonIgnore
    private final int status;
    private final T data;

    private ResponseEntity(int status, T data) {
        this.status = status;
        this.data = data;
    }

    public static <T> ResponseEntity<T> of(int status, T data) {
        return new ResponseEntity<>(status, data);
    }

    public static <T> ResponseEntity<T> ok(T data) {
        return of(ResponseStatus.OK, data);
    }

    public static <T> ResponseEntity<T> created(T data) {
        return of(ResponseStatus.CREATED, data);
    }

}
