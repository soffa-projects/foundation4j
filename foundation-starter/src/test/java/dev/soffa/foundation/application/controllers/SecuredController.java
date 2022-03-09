package dev.soffa.foundation.application.controllers;

import dev.soffa.foundation.annotations.ApplicationRequired;
import dev.soffa.foundation.annotations.Authenticated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Authenticated
public class SecuredController {

    @GetMapping("/secure")
    public String ping() {
        return "Secured";
    }

    @ApplicationRequired
    @GetMapping("/secure/full")
    public String pong() {
        return "Secured";
    }

}
