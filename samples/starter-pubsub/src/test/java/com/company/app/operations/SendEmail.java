package com.company.app.operations;

import dev.soffa.foundation.core.Operation;
import dev.soffa.foundation.extras.mail.models.Email;
import dev.soffa.foundation.extras.mail.models.EmailAck;

public interface SendEmail extends Operation<Email, EmailAck> {
}
