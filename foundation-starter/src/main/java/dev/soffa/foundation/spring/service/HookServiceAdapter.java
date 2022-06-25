package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.commons.*;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.error.ResourceNotFoundException;
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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

    @Override
    public int process(Context context, ProcessHookInput input) {
        try {
            return internalProcess(input);
        } catch (Exception e) {
            Sentry.getInstance().captureException(e);
            throw e;
        }
    }

    @Override
    public void process(Context context, ProcessHookItemInput input) {
        internalProcessItem(input.getType(), input.getSpec(), input.getData());
    }

    @Override
    public void enqueue(@NonNull String hook, @NonNull String subject, @NonNull Map<String, Object> data, @NonNull Context context) {

        Hook lhook = getHook(hook);
        if (NO_HOOK.equals(lhook)) {
            throw new ResourceNotFoundException("Hook not found: " + hook);
        }
        Map<String, Object> ldata = new HashMap<>(data);
        ldata.put("context", context.getContextMap());
        for (HookItem hookItem : lhook.getPost()) {
            scheduler.enqueue(DigestUtil.makeUUID(hookItem.getName() + ":" + subject), ProcessHookItem.class, new ProcessHookItemInput(
                hookItem.getName(),
                hookItem.getType(),
                Mappers.JSON_FULLACCESS_SNAKE.serialize(hookItem.getSpec()),
                Mappers.JSON_FULLACCESS_SNAKE.serialize(ldata)
            ), context);
            LOG.info("Hook queued: %s.%s", hook, hookItem.getName());
        }
    }

    private int internalProcess(ProcessHookInput input) {

        Hook hook = getHook(input.getOperationId());
        if (NO_HOOK.equals(hook)) {
            LOG.warn("No hook found to process:: %s", input.getOperationId());
            return 0;
        }

        int count = 0;
        for (HookItem item : hook.getPost()) {
            LOG.info("Processing hook-item: %s.%s", input.getOperationId(), item.getType());
            internalProcessItem(
                item.getType(),
                Mappers.JSON_FULLACCESS.serialize(item.getSpec()),
                input.getData()
            );
            count++;
        }
        LOG.info("%d hooks processed", count);

        return count;
    }

    public void internalProcessItem(String type, String spec, String data) {
        Map<String, Object> mData = Mappers.JSON_FULLACCESS.deserializeMap(data);
        String tpl = TemplateHelper.render(spec, mData);
        if (Hook.EMAIL.equals(type)) {
            handleEmailHook(Mappers.YAML_FULLACCESS.deserialize(tpl, EmailHook.class));
        } else if (Hook.NOTIFICATION.equals(type)) {
            handleNotificationHook(Mappers.YAML_FULLACCESS.deserialize(tpl, NotificationHook.class));
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
        sender.notify(hook.getMessage(), hook.getContext());
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
