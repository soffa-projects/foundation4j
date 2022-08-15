package dev.soffa.foundation.notification;

import java.util.Map;

public interface Notifier {

    void sendNotification(String uuid, String template, Map<String, Object> vars);

}
