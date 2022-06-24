package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.activity.ActivityService;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.commons.Sentry;
import dev.soffa.foundation.commons.TemplateHelper;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.extra.notifications.NotificationAgent;
import dev.soffa.foundation.hooks.HookService;
import dev.soffa.foundation.hooks.action.ProcessHookItem;
import dev.soffa.foundation.hooks.model.*;
import dev.soffa.foundation.mail.EmailSender;
import dev.soffa.foundation.mail.models.Email;
import dev.soffa.foundation.model.EmailAddress;
import dev.soffa.foundation.scheduling.Scheduler;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class HookServiceAdapter implements HookService {

    private static final Hook NO_HOOK = new Hook();

    private static final Map<String, Hook> HOOKS = new ConcurrentHashMap<>();
    private static final Logger LOG = Logger.get(HookServiceAdapter.class);
    private final ResourceLoader rLoader;
    private final ApplicationContext context;
    private final Scheduler scheduler;
    private final ActivityService activities;

    @Override
    public int process(Context context, ProcessHookInput input) {
        try {
            return internalProcess(context, input);
        } catch (Exception e) {
            Sentry.getInstance().captureException(e);
            throw e;
        }
    }

    @Override
    public void process(Context context, ProcessHookItemInput input) {
        internalProcessItem(context, input.getType(), input.getSpec(), input.getData());
    }

    @Override
    public void enqueue(String operationId, String subject, Map<String, Object> data) {
        Hook hook = getHook(operationId);
        if (NO_HOOK.equals(hook)) {
            return;
        }
        for (HookItem hookItem : hook.getPost()) {
            scheduler.enqueue(ProcessHookItem.class, new ProcessHookItemInput(
                    hookItem.getName(),
                    hookItem.getType(),
                    Mappers.JSON_FULLACCESS_SNAKE.serialize(hookItem.getSpec()),
                    Mappers.JSON_FULLACCESS_SNAKE.serialize(data)
            ));
            LOG.info("Hook queued: %s.%s", operationId, hookItem.getName());
        }
    }

    private int internalProcess(Context context, ProcessHookInput input) {
        Hook hook = getHook(input.getOperationId());
        if (NO_HOOK.equals(hook)) {
            return 0;
        }
        int count = 0;
        for (HookItem item : hook.getPost()) {
            internalProcessItem(
                    context, item.getType(),
                    Mappers.JSON_FULLACCESS.serialize(item.getSpec()),
                    input.getData()
            );
            activities.record(context, Hook.class, null);
            count++;
        }
        return count;
    }

    public void internalProcessItem(Context context, String type, String spec, String data) {
        Map<String, Object> mData = Mappers.JSON_FULLACCESS.deserializeMap(data);
        String tpl = TemplateHelper.render(spec, mData);
        LOG.info("Processing hook [%s]", type);
        if (Hook.EMAIL.equals(type)) {
            handleEmailHook(Mappers.YAML_FULLACCESS.deserialize(tpl, EmailHook.class));
            activities.record(context, EmailHook.class, data);
        } else if (Hook.NOTIFICATION.equals(type)) {
            handleNotificationHook(Mappers.YAML_FULLACCESS.deserialize(tpl, NotificationHook.class));
            activities.record(context, NotificationHook.class, data);
        } else {
            LOG.error("Hook type not supported: %s", type);
        }
    }


    private void handleEmailHook(EmailHook model) {
        EmailSender sender = context.getBean(EmailSender.class);
        Email email = Email.builder().to(EmailAddress.of(model.getTo())).subject(model.getSubject()).htmlMessage(model.getBody()).build();
        sender.send(email);

    }

    private void handleNotificationHook(NotificationHook hook) {
        NotificationAgent sender = context.getBean(NotificationAgent.class);
        sender.notify(hook.getMessage());
    }


    @SuppressWarnings("Convert2Lambda")
    @SneakyThrows
    @Override
    public Hook getHook(String operationId) {

        return HOOKS.computeIfAbsent(operationId, new Function<String, Hook>() {
            @SneakyThrows
            @Override
            public Hook apply(String s) {
                Hook hook = NO_HOOK;
                Resource res = rLoader.getResource("classpath:/hooks/" + operationId + ".yml");
                if (res.exists()) {
                    hook = Mappers.YAML.deserialize(res.getInputStream(), Hook.class);
                } else {
                    LOG.info("No hook registered for operation: %s", operationId);
                }
                return hook;
            }
        });
    }
}
