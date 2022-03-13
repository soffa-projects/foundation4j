package dev.soffa.foundation.starter.test.app.operation;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.Data;

@Data
public class UpdateContentInput {

    @Hidden
    private String id;
    private String content;
}
