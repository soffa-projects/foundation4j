package dev.soffa.foundation.starter.test.app.resource;

import dev.soffa.foundation.annotation.ApplicationRequired;
import dev.soffa.foundation.annotation.Authenticated;
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
