package dev.soffa.foundation.commons;

import lombok.Data;

import java.util.List;

@Data
public class JwtToken {

    private List<String> audience;
    private String header;
    private String subject;
    private String keyId;
    private String issuer;
    private String type;
}
