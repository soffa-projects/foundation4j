package dev.soffa.foundation.starter.test;

import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.commons.UrlInfo;
import dev.soffa.foundation.mail.EmailSender;
import dev.soffa.foundation.mail.adapter.FakeEmailSender;
import dev.soffa.foundation.mail.models.Email;
import dev.soffa.foundation.model.EmailAddress;
import dev.soffa.foundation.support.mail.EmailSenderFactory;
import dev.soffa.foundation.support.mail.SendgridEmailSender;
import dev.soffa.foundation.support.mail.SmtpEmailSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EmailTest {

    public static final String NO_REPLY = "noreply@local.dev";

    @Test
    public void testEmailAddress() {

        String address = NO_REPLY;

        EmailAddress email = new EmailAddress(TextUtil.format(" %s ", NO_REPLY));
        assertNull(email.getName());
        assertEquals(address, email.getAddress());

        email = new EmailAddress(TextUtil.format(" <%s> ", NO_REPLY));
        Assertions.assertTrue(TextUtil.isEmpty(email.getName()));
        assertEquals(address, email.getAddress());

        email = new EmailAddress(TextUtil.format("John Doe <%s> ", NO_REPLY));
        assertEquals("John Doe", email.getName());
        assertEquals(address, email.getAddress());

        email = new EmailAddress(TextUtil.format("\"John Doe\" <%s> ", NO_REPLY));
        assertEquals("John Doe", email.getName());
        assertEquals(address, email.getAddress());

    }

    @Test
    public void testFakeEmailSender() {
        int counter = FakeEmailSender.getCounter();
        EmailSender sender = new FakeEmailSender();
        sender.send(
            new Email("Hello mailer", EmailAddress.of(NO_REPLY), "Hello world!", null)
        );
        assertEquals(counter + 1, FakeEmailSender.getCounter());
    }


    @Test
    public void testUrlParsing() {
        UrlInfo url = UrlInfo.parse("smtp://user:pass@mail.google.com:963?tls=true");
        assertNotNull(url);
        Assertions.assertEquals("smtp", url.getProtocol());
        assertNotNull(url.getParams());
        Assertions.assertEquals("user", url.getUsername());
        Assertions.assertEquals("pass", url.getPassword());
        Assertions.assertEquals("mail.google.com", url.getHostname());
        Assertions.assertEquals(963, url.getPort());
        Assertions.assertEquals("true", url.param("tls", null));
    }

    @Test
    public void testEmailSenderFactory() {
        EmailSender sender = EmailSenderFactory.create("smtp://user:pass@mail.google.com:963?tls=true", NO_REPLY);
        assertNotNull(sender);
        assertEquals(SmtpEmailSender.class, sender.getClass());

        sender = EmailSenderFactory.create("faker://local", NO_REPLY);
        assertNotNull(sender);
        assertEquals(FakeEmailSender.class, sender.getClass());

        sender = EmailSenderFactory.create("sendgrid://ak092092012:@sendgrid.com", NO_REPLY);
        assertNotNull(sender);
        assertEquals(SendgridEmailSender.class, sender.getClass());

        sender = EmailSenderFactory.create("sendgrid://sendgrid.com?apiKey=ak092092012", NO_REPLY);
        assertNotNull(sender);
        assertEquals(SendgridEmailSender.class, sender.getClass());
    }


}
