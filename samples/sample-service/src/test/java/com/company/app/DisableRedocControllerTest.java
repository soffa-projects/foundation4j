package com.company.app;

import dev.soffa.foundation.test.spring.HttpExpect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest(properties = {"app.redoc.enabled=false"})
@AutoConfigureMockMvc
public class DisableRedocControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void homepageShouldReturn404WhenRedocIsDisabled() {
        HttpExpect test = new HttpExpect(mvc);
        test.get("/").expect().is4xxClientError();
    }
}
