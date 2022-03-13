package dev.soffa.foundation.starter.test;

import dev.soffa.foundation.mail.Mailer;
import dev.soffa.foundation.mail.adapter.FakeEmailSender;
import dev.soffa.foundation.mail.adapter.SendgridEmailSender;
import dev.soffa.foundation.mail.adapter.SmtpEmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "app.mail.clients.default=smtp://foo:s3cret@smtp.google.com:587",
    "app.mail.clients.faker=faker://local",
    "app.mail.clients.sendgrid=sendgrid://sendgrid.com?apiKey=12121212",
})
@ActiveProfiles("test")
public class EmailAutoConfigurationTest {

    @Autowired
    private Mailer mailer;

    @Test
    public void testMailerConfig() {
        assertNotNull(mailer);
        assertTrue(mailer.getClient("default") instanceof SmtpEmailSender);
        assertTrue(mailer.getClient("faker") instanceof FakeEmailSender);
        assertTrue(mailer.getClient("sendgrid") instanceof SendgridEmailSender);
    }

}
