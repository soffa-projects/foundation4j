package com.company.app.core.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;


@Getter
@Setter
public class InputData {

    @NotEmpty(message = "Please enter a username")
    private String username;

    @NotEmpty(message = "Please enter a password")
    private String password;

}
