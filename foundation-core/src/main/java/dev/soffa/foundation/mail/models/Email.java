package dev.soffa.foundation.mail.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.soffa.foundation.model.EmailAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Email {

    private String subject;
    private EmailAddress sender;
    private List<EmailAddress> to;
    private List<EmailAddress> cc;
    private List<EmailAddress> bcc;
    private String textMessage;
    private String htmlMessage;
    private List<Attachment> attachments;

    public Email(String subjet, EmailAddress sender, List<EmailAddress> to, String textMessage, String htmlMessage) {
        this.subject = subjet;
        this.sender = sender;
        this.to = to;
        this.textMessage = textMessage;
        this.htmlMessage = htmlMessage;
    }

    public Email(String subject, List<EmailAddress> to, String textMessage, String htmlMessage) {
        this.subject = subject;
        this.to = to;
        this.textMessage = textMessage;
        this.htmlMessage = htmlMessage;
    }

    public Email(String subjet, EmailAddress to, String textMessage, String htmlMessage) {
        this.subject = subjet;
        this.to = Collections.singletonList(to);
        this.textMessage = textMessage;
        this.htmlMessage = htmlMessage;
    }

    @JsonIgnore
    public boolean hasMessage() {
        if (StringUtils.isNotEmpty(textMessage)) {
            return true;
        }
        return StringUtils.isNotEmpty(htmlMessage);
    }

}
