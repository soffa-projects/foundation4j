package dev.soffa.foundation;

import dev.soffa.foundation.commons.TextUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TextUtilTest {

    @Test
    void testCase() {
        assertEquals("hello_world", TextUtil.snakeCase(" Hello    World "));
        assertEquals("hello_out_there_can_you_hear_me", TextUtil.snakeCase(" hello Out there Can you Hear me"));
    }

    @Test
    void testTrimToNull() {
        assertNull(TextUtil.trimToNull(" "));
        assertNull(TextUtil.trimToNull(null));
        assertNotNull(TextUtil.trimToNull("Hello"));
    }

    @Test
    void testTrimToEmpty() {
        assertEquals("", TextUtil.trimToEmpty(null));
        assertEquals("", TextUtil.trimToEmpty("  "));
        assertEquals("Hello", TextUtil.trimToEmpty("Hello  "));
    }
}
