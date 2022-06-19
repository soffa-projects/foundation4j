package dev.soffa.foundation.starter.test.app;

import dev.soffa.foundation.resource.Resource;
import dev.soffa.foundation.starter.test.app.handlers.Echo;
import dev.soffa.foundation.starter.test.app.handlers.GetMessages;
import dev.soffa.foundation.starter.test.app.handlers.UpdateContent;
import dev.soffa.foundation.starter.test.app.handlers.UpdateContent2;
import dev.soffa.foundation.starter.test.app.model.EchoInput;
import dev.soffa.foundation.starter.test.app.model.Message;
import dev.soffa.foundation.starter.test.app.model.UpdateContentInput;
import dev.soffa.foundation.starter.test.app.operation.EchoSecured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tags(
    @Tag(name = "Echo", description = "All things echo messaging")
)
@RestController
//@Generated
public interface EchoResource extends Resource {

    @Operation(
        summary = "Echo the input message"
    )
    @PostMapping("/v1/echo")
    default Message echo(@RequestBody EchoInput input) {
        return invoke(Echo.class, input);
    }

    @Operation(
        summary = "Echo the input message"
    )
    @PostMapping("/v1/echo/secured")
    default Message echoSecured(@RequestBody EchoInput input) {
        return invoke(EchoSecured.class, input);
    }

    @PatchMapping("/v1/messages/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    default Message updateContent(@PathVariable String id, @RequestBody UpdateContentInput input) {
        input.setId(id);
        return invoke(UpdateContent.class, input);
    }

    @PatchMapping("/v1.1/messages/{id}")
    default Message updateContent2(@PathVariable String id) {
        return invoke(UpdateContent2.class, id);
    }

    @GetMapping("/v1.1/messages")
    default List<Message> getMessages() {
        return invoke(GetMessages.class, null);
    }

    @GetMapping("/v1.2/messages")
    default List<Message> getMessagesV2() {
        return invoke(GetMessages.class, null);
    }

}
