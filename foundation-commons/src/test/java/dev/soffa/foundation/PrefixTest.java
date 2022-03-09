package dev.soffa.foundation;

import dev.soffa.foundation.commons.Prefix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrefixTest {

    public static final String CH = "ch";
    public static final String CH_LIVE = "ch_live";

    @Test
    public void testPrefix() {
        Prefix prefix = new Prefix();
        prefix.append(CH);
        assertEquals(CH, prefix.getValue());
        prefix.append("live");
        assertEquals(CH_LIVE, prefix.getValue());

        prefix.appendIf(false, "test");
        assertEquals(CH_LIVE, prefix.getValue());

        prefix.appendIf(true, "2");
        assertEquals("ch_live_2", prefix.getValue());
    }
}
