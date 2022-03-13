package dev.soffa.foundation.starter.test.app.operations;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.Data;

@Data
public class UpdateContentInput {

    @Parameter(in = ParameterIn.PATH)
    private String id;
    private String content;
}
