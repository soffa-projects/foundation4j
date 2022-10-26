package dev.soffa.foundation.commons;

import java.time.Duration;

public interface Durations {

    long D_1_SEC_VALUE = 6000;

    long D_5_SEC_VALUE = D_1_SEC_VALUE * 5;
    Duration D_5S = Duration.ofSeconds(5);

    long D_10_SEC_VALUE = D_1_SEC_VALUE * 10;
    Duration D_10_SEC = Duration.ofSeconds(10);

    long D_15_SEC_VALUE = D_1_SEC_VALUE * 15;
    Duration D_15_SEC = Duration.ofSeconds(15);

    long D_30_SEC_VALUE = D_1_SEC_VALUE * 30;
    Duration D_30_SEC = Duration.ofSeconds(30);

    long D_1_MIN_VALUE = D_1_SEC_VALUE * 60;
    Duration D_1_MIN = Duration.ofMinutes(1);

    long D_5_MIN_VALUE = D_1_MIN_VALUE * 5;
    Duration D_5_MIN = Duration.ofMinutes(5);

    long D_10_MIN_VALUE = D_1_MIN_VALUE * 10;
    Duration D_10_MIN = Duration.ofMinutes(10);

    long D_15_MIN_VALUE = D_1_MIN_VALUE * 15;
    Duration D_15_MIN = Duration.ofMinutes(15);

    long D_30_MIN_VALUE = D_1_MIN_VALUE * 30;
    Duration D_30_MIN = Duration.ofMinutes(30);

    long D_1_H_VALUE = D_1_MIN_VALUE * 60;
    Duration D_1_H = Duration.ofHours(1);

}
