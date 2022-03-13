package dev.soffa.foundation.test;

import dev.soffa.foundation.messages.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SuppressWarnings({"PMD.AbstractClassWithoutAbstractMethod", "PMD.AbstractClassWithoutAnyMethod"})
public abstract class BaseMessageTest {

    @Autowired
    protected MessageHandler messenger;

}
