package dev.soffa.foundation.commons;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class EventBus {

    private static final com.google.common.eventbus.EventBus BUS = new com.google.common.eventbus.EventBus("default");

    private EventBus() {
    }

    public static void register(Object target) {
        BUS.register(target);
    }

    public static void unregister(Object target) {
        BUS.unregister(target);
    }

    public static void post(Object event) {
        BUS.post(event);
    }

}
