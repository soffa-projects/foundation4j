package dev.soffa.foundation.mail.adapters;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.errors.TechnicalException;
import dev.soffa.foundation.extras.mail.EmailSender;
import dev.soffa.foundation.extras.mail.models.EmailAck;
import dev.soffa.foundation.models.EmailAddress;
import dev.soffa.foundation.models.ResponseStatus;
import lombok.SneakyThrows;

import java.util.List;
import java.util.stream.Collectors;


public class SendgridEmailSender implements EmailSender {

    private static final Logger LOG = Logger.get(SendgridEmailSender.class);
    private final String defaultSender;
    private final SendGrid sendgrid;


    public SendgridEmailSender(String apiKey, String defaultSender) {
        this.defaultSender = defaultSender;
        if (TextUtil.isEmpty(apiKey)) {
            throw new TechnicalException("Sendgrid API key is not configured");
        }
        this.sendgrid = new SendGrid(apiKey);
    }

    @SneakyThrows
    @Override
    public EmailAck send(dev.soffa.foundation.extras.mail.models.Email message) {
        EmailAddress sender = new EmailAddress(defaultSender);
        com.sendgrid.helpers.mail.objects.Email from = new Email(sender.getAddress(), sender.getName());

        List<Email> recipients = message.getTo().stream().map((it) -> {
            // EL
            return new Email(it.getAddress(), it.getName());
        }).collect(Collectors.toList());

        String subject = message.getSubject();
        Content content = new Content("text/html", message.getHtmlMessage());

        Personalization p = new Personalization();
        Mail mail = new Mail(from, subject, recipients.get(0), content);
        boolean hasMoreRecipients = recipients.size() > 1;
        if (hasMoreRecipients) {
            for (int i = 1; i < recipients.size(); i++) {
                p.addTo(recipients.get(i));
            }
            mail.addPersonalization(p);
        }

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendgrid.api(request);
        if (response.getStatusCode() >= ResponseStatus.BAD_REQUEST) {
            throw new TechnicalException("Failed to send email: ${response.statusCode} ${response.body}");
        }
        LOG.info("Email successfully sent to %s", recipients.get(0).getEmail());
        return new EmailAck("OK", "00");
    }
}
