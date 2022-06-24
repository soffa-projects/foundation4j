package dev.soffa.foundation.extra.notifications;


import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface NotificationAgent {

    AtomicLong COUNTER = new AtomicLong(0);
    void notify(String message, Map<String,String> context);

}
