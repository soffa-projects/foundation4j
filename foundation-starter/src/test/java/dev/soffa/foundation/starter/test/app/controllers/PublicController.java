package dev.soffa.foundation.starter.test.app.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicController {

    @GetMapping("/public")
    public String hello() {
        return "Public";
    }

}
