package dev.soffa.foundation.commons;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtil {

    private RandomUtil() {
    }

    public static String nextString(String... candidates) {
        return candidates[nextInt(0, candidates.length - 1)];
    }

    public static String nextString() {
        return RandomStringUtils.random(32, true, true);
    }

    public static String nextString(int count) {
        return RandomStringUtils.random(count, true, true);
    }

    public static int nextInt() {
        return RandomUtils.nextInt();
    }

    public static int nextInt(int startInclusive, int endExclusive) {
        return RandomUtils.nextInt(startInclusive, endExclusive);
    }

    public static Date nextDate(Instant startInclusive, Instant endExclusive) {
        long startSeconds = startInclusive.getEpochSecond();
        long endSeconds = endExclusive.getEpochSecond();
        long random = ThreadLocalRandom
            .current()
            .nextLong(startSeconds, endSeconds);

        return Date.from(Instant.ofEpochSecond(random));
    }

}
