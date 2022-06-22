package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.commons.Sentry;
import dev.soffa.foundation.commons.TemplateHelper;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.extra.notifications.NotificationAgent;
import dev.soffa.foundation.hooks.HookService;
import dev.soffa.foundation.hooks.action.ProcessHookInput;
import dev.soffa.foundation.hooks.model.EmailHook;
import dev.soffa.foundation.hooks.model.Hook;
import dev.soffa.foundation.hooks.model.HookItem;
import dev.soffa.foundation.hooks.model.NotificationHook;
import dev.soffa.foundation.mail.EmailSender;
import dev.soffa.foundation.mail.models.Email;
import dev.soffa.foundation.model.EmailAddress;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class HookServiceAdapter implements HookService {

    private static final Hook NO_HOOK = new Hook();

    private static final Map<String, Hook> hooks = new ConcurrentHashMap<>();
    private static final Logger LOG = Logger.get(HookServiceAdapter.class);
    private final ResourceLoader rLoader;
    private final ApplicationContext context;

    public int process(Context context, ProcessHookInput input) {
        try {
            return internalProcess(context, input);
        } catch (Exception e) {
            Sentry.getInstance().captureException(e);
            throw e;
        }
    }

    private int internalProcess(Context context, ProcessHookInput input) {
        Hook hook = getHook(input.getOperationId());
        if (hook == null) {
            return 0;
        }
        int count = 0;
        for (HookItem item : hook.getPost()) {
            String tpl = Mappers.YAML.serialize(item.getSpec());
            tpl = TemplateHelper.render(tpl, input.getData());
            if (Hook.EMAIL.equals(item.getType())) {
                handleEmailHook(context, Mappers.YAML.deserialize(tpl, EmailHook.class));
            } else if (Hook.NOTIFICATION.equals(item.getType())) {
                handleNotificationHook(context, Mappers.YAML.deserialize(tpl, NotificationHook.class));
            } else {
                LOG.warn("Hook type not supported: %s", item.getType());
            }
            count++;
        }
        return count;
    }

    private void handleEmailHook(Context ctx, EmailHook model) {
        EmailSender sender = context.getBean(EmailSender.class);
        Email email = Email.builder().to(EmailAddress.of(model.getTo())).subject(model.getSubject()).htmlMessage(model.getBody()).build();
        sender.send(email);
    }

    private void handleNotificationHook(Context ctx, NotificationHook hook) {
        NotificationAgent sender = context.getBean(NotificationAgent.class);
        sender.notify(hook.getMessage());
    }


    @SneakyThrows
    public Hook getHook(String operationId) {
        Resource res = rLoader.getResource("classpath:/hooks/" + operationId + ".yml");
        return hooks.computeIfAbsent(operationId, s -> {
            Hook hook = NO_HOOK;
            if (res.exists()) {
                try {
                    hook = Mappers.YAML.deserialize(res.getInputStream(), Hook.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                LOG.info("No hook registered for operation: %s", operationId);
            }
            return hook;
        });
    }
}
