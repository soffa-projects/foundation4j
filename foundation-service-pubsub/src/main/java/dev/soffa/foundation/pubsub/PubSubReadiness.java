package dev.soffa.foundation.pubsub;

import dev.soffa.foundation.errors.TechnicalException;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class PubSubReadiness {

    private static final AtomicBoolean READY = new AtomicBoolean(false);

    private PubSubReadiness() {
    }

    public static void setReady() {
        READY.set(true);
    }

    public static boolean isReady() {
        return READY.get();
    }

    @SneakyThrows
    public static void await(Duration atMost) {
        if (isReady()) {
            return;
        }
        Duration duration = atMost;
        while (!isReady()) {
            //noinspection BusyWait
            Thread.sleep(500);
            if (isReady()) {
                return;
            }
            duration = duration.minus(Duration.ofMillis(500));
            if (duration.isZero() || duration.isNegative()) {
                break;
            }
        }
        if (isReady()) {
            return;
        }
        throw new TechnicalException("Timeout while waiting for database plane to be ready");
    }

}
