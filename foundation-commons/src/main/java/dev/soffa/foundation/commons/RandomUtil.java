package dev.soffa.foundation.commons;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public final class RandomUtil {

    private RandomUtil() {
    }

    public static String nextString() {
        return RandomStringUtils.random(32, true, true);
    }

    public static int nextInt() {
        return RandomUtils.nextInt();
    }

    public static int nextInt(int startInclusive, int endExclusive) {
        return RandomUtils.nextInt(startInclusive, endExclusive);
    }

}
