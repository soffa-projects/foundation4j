package dev.soffa.foundation.starter.test.app;

import dev.soffa.foundation.annotations.Resource;
import dev.soffa.foundation.models.ResponseEntity;
import dev.soffa.foundation.starter.test.app.models.Message;
import dev.soffa.foundation.starter.test.app.operations.EchoInput;
import dev.soffa.foundation.starter.test.app.operations.UpdateContentInput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import java.util.Collections;
import java.util.List;

@Tags(
    @Tag(name = "Echo", description = "All things echo messaging")
)
@Path("")
@Resource
public interface EchoResource {

    @Operation(
        method = "POST",
        summary = "Echo the input message",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Echoed message"
            )
        }
    )
    @Path("/v1/echo")
    Message echo(@RequestBody EchoInput input);

    @PATCH
    @Path("/v1/messages/{id}")
    ResponseEntity<Message> updateContent(@RequestBody UpdateContentInput input);

    @PATCH
    @Path("/v1.1/messages/{id}")
    Message updateContent2(String id);

    @GET
    @Path("/v1.1/messages")
    List<Message> getMessages();

    @GET
    @Path("/v1.2/messages")
    default List<Message> getMessagesV2() {
        return Collections.emptyList();
    }

}
