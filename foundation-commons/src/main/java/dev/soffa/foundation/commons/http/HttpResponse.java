package dev.soffa.foundation.commons.http;

import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

import static dev.soffa.foundation.models.ResponseStatus.*;

@Data
@Builder
public class HttpResponse {

    private int status;
    private String message;
    private String contentType;
    private String body;

    public static HttpResponse ok(String contentType, String body) {
        return HttpResponse.builder().status(OK).contentType(contentType).body(body).build();
    }

    public static HttpResponse notFound() {
        return HttpResponse.builder().status(NOT_FOUND).build();
    }

    public String getMessageOrBody() {
        return message;
    }

    @SneakyThrows
    public boolean isOK() {
        return status == OK;
    }

    @SneakyThrows
    public boolean isBadRequest() {
        return status == BAD_REQUEST;
    }

    @SneakyThrows
    public boolean is(int statusCode) {
        return statusCode == status;
    }

    @SneakyThrows
    public boolean is2xxSuccessful() {
        return status >= OK && status < MULTIPLE_CHOICES;
    }

    @SneakyThrows
    public boolean is3xxRedirection() {
        return status >= MULTIPLE_CHOICES && status < BAD_REQUEST;
    }

    @SneakyThrows
    public boolean is4xxClientError() {
        return status >= BAD_REQUEST && status < SERVER_ERROR;
    }

    @SneakyThrows
    public boolean is5xxServerError() {
        return status >= SERVER_ERROR;
    }

    @SneakyThrows
    public boolean isForbidden() {
        return status == FORBIDDEN;
    }

    @SneakyThrows
    public boolean isUnauthorized() {
        return status == UNAUTHORIZED;
    }

    /*
    public <T> T json(String path) {
        try {
            return JsonPath.read(getBody(), path);
        } catch (PathNotFoundException e) {
            return null;
        }
    }
    */


}
