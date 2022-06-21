package dev.soffa.foundation.openapi;

import io.swagger.v3.oas.models.info.Contact;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OpenAPIDesc {

    private String version = "3.0.1";
    private Info info;
    private Security security;
    private String servers;
    private List<Parameter> parameters;

    @Data
    @NoArgsConstructor
    public static class Info {
        private String title;
        private String description;
        private String version;
        private Contact contact;
    }

    @Data
    @NoArgsConstructor
    public static class Security {
        private OAuth2 oAuth2;
        private boolean bearerAuth;
        private boolean basicAuth;
    }

    @Data
    @NoArgsConstructor
    public static class OAuth2 {
        private String url;
        private String scopes;
        private boolean authorizationCodeFlow = true;
        private boolean passwordFlow;
    }

    @Data
    @NoArgsConstructor
    public static class Parameter {
        private String name;
        private String in;
        private String description;
        private List<String> values;
        private String value;
        private String type = "string";
        private boolean required;
    }

}
