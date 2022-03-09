package dev.soffa.foundation.mail.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailAck {

    private String status;
    private String messageId;

}
