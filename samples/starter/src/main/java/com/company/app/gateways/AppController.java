package com.company.app.gateways;

import com.company.app.core.Echo;
import com.company.app.core.Ping;
import com.company.app.core.PingResponse;
import com.company.app.core.model.InputData;
import dev.soffa.foundation.context.Context;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class AppController implements API {

    private final Ping pingOp;
    private final Echo echoOp;

    @Override
    @GetMapping("ping")
    public PingResponse ping(Context context) {
        return pingOp.handle(context);
    }

    @Override
    @PostMapping("echo")
    public String echo(@RequestBody String input, Context context) {
        return echoOp.handle(input, context);
    }

    @PostMapping("check")
    public String check(@Valid @RequestBody InputData input) {
        return input.getUsername();
    }

    /*
    @PostMapping("check/{id}")
    public ResponseEntity<String> check(@PathVariable String id) {
        return ResponseEntity.created(id);
    }
    */

}
