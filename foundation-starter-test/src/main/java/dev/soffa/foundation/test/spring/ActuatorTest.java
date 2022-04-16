package dev.soffa.foundation.test.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SuppressWarnings({"PMD.AbstractClassWithoutAbstractMethod", "SpringJavaAutowiredMembersInspection"})
public class ActuatorTest extends ApplicationContextTest {

    @Autowired
    private MockMvc mvc;

    //@Value("${server.servlet.contextPath}")
    //private String contextPath;

    @Test
    public void testActuator() {
        HttpExpect http = new HttpExpect(mvc);
        http.get("/actuator/health").expect().isOK().json("status", "UP");
    }



}
