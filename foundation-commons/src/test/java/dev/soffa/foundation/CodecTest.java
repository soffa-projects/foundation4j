package dev.soffa.foundation;

import dev.soffa.foundation.commons.DigestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.github.javaparser.utils.Utils.assertNotNull;

public class CodecTest {

    @Test
    public void testUUIDFromString() {
        String raw = "payment_intent.pi_gPJQZArmZ2oE.post_process";
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            //noinspection ResultOfMethodCallIgnored
            UUID.fromString(raw);
        });
        String uuid0 = DigestUtil.md5(raw);
        uuid0 = uuid0.substring(0, 8) + "-" + uuid0.substring(8, 12) + "-" + uuid0.substring(12, 16) + "-" + uuid0.substring(16, 20) + "-" + uuid0.substring(20);
        UUID uuid = UUID.fromString(uuid0);
        assertNotNull(uuid);

    }

}
