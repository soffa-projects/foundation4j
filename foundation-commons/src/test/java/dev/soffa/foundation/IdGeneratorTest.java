package dev.soffa.foundation;

import dev.soffa.foundation.commons.IdGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IdGeneratorTest {

    public static final String CH = "ch";
    public static final String LIVE = "live";
    public static final String LIVE_ = "_" + LIVE;
    public static final String CH_ = CH + "_";
    public static final String CH_LIVE = "ch_live_";

    @Test
    public void testIdGenerator() {
        assertTrue(IdGenerator.generate(CH).startsWith(CH_));
        assertTrue(IdGenerator.generate(CH_).startsWith(CH_));

        assertTrue(IdGenerator.generate(CH, LIVE).startsWith(CH_LIVE));
        assertTrue(IdGenerator.generate(CH, LIVE_).startsWith(CH_LIVE));
        assertTrue(IdGenerator.generate(CH_, LIVE_).startsWith(CH_LIVE));
        assertTrue(IdGenerator.generate(CH_, LIVE).startsWith(CH_LIVE));
    }

}
