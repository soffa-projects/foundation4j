package dev.soffa.foundation.pubsub;

import dev.soffa.foundation.commons.EventBus;
import dev.soffa.foundation.message.Message;
import dev.soffa.foundation.message.MessageHandler;
import dev.soffa.foundation.message.pubsub.PubSubClient;
import dev.soffa.foundation.message.pubsub.PubSubMessenger;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PubSubMessengerImpl implements PubSubMessenger {

    public static final String DEFAULT = "default";
    private final Map<String, PubSubClient> clients;
    private final PubSubClient defaultClient;

    public PubSubMessengerImpl(Map<String, PubSubClient> clients) {
        this.clients = clients;
        if (clients.containsKey(DEFAULT)) {
            defaultClient = clients.get(DEFAULT);
        } else {
            defaultClient = clients.values().iterator().next();
        }
        EventBus.register(this);
    }


    @Override
    public PubSubClient getDefaultClient() {
        return defaultClient;
    }

    @Override
    public PubSubClient getClient(String name) {
        return clients.get(name);
    }

    @Override
    public void subscribe(@NonNull String subject, boolean broadcast, MessageHandler messageHandler) {
        getDefaultClient().subscribe(subject, broadcast, messageHandler);
    }

    @Override
    public void subscribe(MessageHandler messageHandler) {
        getDefaultClient().subscribe(messageHandler);
    }

    @Override
    public <T> CompletableFuture<T> request(@NonNull String subject, @NotNull Message message, Class<T> expectedClass) {
        return getDefaultClient().request(subject, message, expectedClass);
    }

    @Override
    public void publish(@NonNull String subject, @NotNull Message message) {
        getDefaultClient().publish(subject, message);
    }

    @Override
    public void publish(@NotNull Message message) {
        getDefaultClient().publish(message);
    }

    @Override
    public void broadcast(@NonNull String target, @NotNull Message message) {
        getDefaultClient().broadcast(target, message);
    }

    @Override
    public void setDefaultBroadcast(String value) {
        getDefaultClient().setDefaultBroadcast(value);
    }

    /*
    @Subscribe
    public void onDatabaseReady(DatabaseReadyEvent ignore) {
        EventBus.unregister(this);
        for (Map.Entry<String, PubSubClientConfig> e : config.getClients().entrySet()) {
            String subjects = e.getValue().getSubjects();
            configureListeners(clients.get(e.getKey()), subjects);
        }
        PubSubReadiness.setReady();
    }

    private void configureListeners(PubSubClient client, String subjects) {
        if (TextUtil.isEmpty(subjects)) {
            return;
        }
        String[] subs = subjects.split(",");
        for (String sub : subs) {
            if (TextUtil.isNotEmpty(sub)) {
                boolean isBroadcast = sub.endsWith("*");
                String rsub = sub.replaceAll("\\*", "");
                if (isBroadcast) {
                    client.setDefaultBroadcast(rsub);
                }
                client.subscribe(rsub, isBroadcast, messageHandler);
            }
        }
    }

     */

}
