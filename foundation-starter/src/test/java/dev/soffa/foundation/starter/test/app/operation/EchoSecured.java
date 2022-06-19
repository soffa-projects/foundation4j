package dev.soffa.foundation.starter.test.app.operation;

import dev.soffa.foundation.annotation.DefaultTenant;
import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.starter.test.app.model.EchoInput;
import dev.soffa.foundation.starter.test.app.model.Message;
import org.springframework.security.access.annotation.Secured;

@Secured("admin")
@DefaultTenant
public interface EchoSecured extends Operation<EchoInput, Message> {
}
