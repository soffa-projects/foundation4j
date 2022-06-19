package dev.soffa.foundation.spring.handler;

import dev.soffa.foundation.application.action.SendNotification;
import dev.soffa.foundation.commons.TemplateHelper;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.extra.notifications.NotificationAgent;
import dev.soffa.foundation.model.TemplateMessage;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DoSendNotification implements SendNotification {

    private final ResourceLoader rLoader;
    private final NotificationAgent notifier;

    @SneakyThrows
    @Override
    public Void handle(TemplateMessage input, @NonNull Context ctx) {
        Resource resource = rLoader.getResource("classpath:/templates/" + input.getTemplate());
        String message = input.getTemplate();
        if (resource.exists()) {
            message = TemplateHelper.render(resource.getInputStream(), input.getValues());
        } else {
            message = TemplateHelper.render(message, input.getValues());
        }
        notifier.notify(message);
        return null;
    }

}
