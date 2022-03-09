package dev.soffa.foundation.test;

import org.awaitility.Awaitility;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TestUtil {

    public static void awaitUntil(int seconds, Supplier<Boolean> tester) {
        Awaitility.await().atMost(seconds, TimeUnit.SECONDS).until(tester::get);
    }

}
