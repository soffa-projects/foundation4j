package com.company.app.gateways;

import com.fasterxml.jackson.annotation.JsonValue;
import dev.soffa.foundation.models.VO;
import lombok.Value;

import javax.persistence.Column;

@Value
public class MessageId implements VO {

    private static final long serialVersionUID = 1L;

    @JsonValue
    @Column(name = "id")
    String value;

}
