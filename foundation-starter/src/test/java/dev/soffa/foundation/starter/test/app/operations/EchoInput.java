package dev.soffa.foundation.starter.test.app.operations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EchoInput {

    @NotEmpty
    private String message;

}
