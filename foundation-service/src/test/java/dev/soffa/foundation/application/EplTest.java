package dev.soffa.foundation.application;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.TemplateHelper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EplTest {

    @Test
    public void testEpl() {
        String expression = "partner={{ arg0.partnerId }}";
        Map<String, Object> root = ImmutableMap.of("arg0", ImmutableMap.of("partnerId", "Black Panther"));

        String output2 = TemplateHelper.render(expression, root);
        assertEquals("partner=Black Panther", output2);

        //ExpressionParser epl = new SpelExpressionParser();
        // assertEquals("partner=Black Panther", output2);


    }

}
