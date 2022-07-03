package dev.soffa.foundation.spring.service;

import dev.soffa.foundation.commons.*;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.core.HookService;
import dev.soffa.foundation.core.action.ProcessHookItem;
import dev.soffa.foundation.core.model.*;
import dev.soffa.foundation.error.ResourceNotFoundException;
import dev.soffa.foundation.extra.notifications.NotificationAgent;
import dev.soffa.foundation.mail.EmailSender;
import dev.soffa.foundation.mail.models.Email;
import dev.soffa.foundation.model.EmailAddress;
import dev.soffa.foundation.scheduling.OperationScheduler;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class HookServiceAdapter implements HookService {

    private static final HookSpec NO_HOOK = new HookSpec();

    private static final Map<String, HookSpec> HOOKS = new ConcurrentHashMap<>();
    private static final Logger LOG = Logger.get(HookServiceAdapter.class);
    private final ResourceLoader rLoader;
    private final ApplicationContext context;
    private final OperationScheduler scheduler;

    private final AtomicLong processedHooks = new AtomicLong(0);

    @Override
    public int process(Context context, ProcessHookInput input) {
        try {
            return internalProcess(input);
        } catch (Exception e) {
            Sentry.get().captureException(e);
            throw e;
        }
    }

    @Override
    public void process(Context context, ProcessHookItemInput input) {
        internalProcessItem(input.getType(), input.getSpec(), input.getData());
    }

    @Override
    public long getProcessedHooks() {
        return processedHooks.get();
    }

    @Override
    public void enqueue(@NonNull String hook, @NonNull String subject, @NonNull Map<String, Object> data, @NonNull Context context) {
        HookSpec lhook = getHook(hook);
        if (NO_HOOK.equals(lhook)) {
            throw new ResourceNotFoundException("Hook not found: " + hook);
        }
        Map<String, Object> ldata = new HashMap<>(data);
        ldata.put("__context", context);
        for (HookItemSpec hookItem : lhook.getPost()) {
            String uuid = hook + ":" + subject + ":" + hookItem.getName();
            scheduler.enqueue(DigestUtil.makeUUID(uuid), ProcessHookItem.class, new ProcessHookItemInput(
                hook,
                hookItem.getName(),
                hookItem.getType(),
                Mappers.JSON_DEFAULT.serialize(hookItem.getSpec()),
                Mappers.JSON_DEFAULT.serialize(ldata)
            ), context);
            LOG.info("Hook queued: %s.%s [%s]", hook, hookItem.getName(), uuid);
        }
    }

    private int internalProcess(ProcessHookInput input) {

        HookSpec hook = getHook(input.getOperationId());
        if (NO_HOOK.equals(hook)) {
            LOG.warn("No hook found to process:: %s", input.getOperationId());
            return 0;
        }

        int count = 0;
        for (HookItemSpec item : hook.getPost()) {
            internalProcessItem(
                item.getType(),
                Mappers.JSON_DEFAULT.serialize(item.getSpec()),
                input.getData()
            );
            count++;
        }
        return count;
    }

    public void internalProcessItem(String type, String spec, String data) {
        processedHooks.incrementAndGet();
        Map<String, Object> mData = Mappers.JSON_DEFAULT.deserializeMap(data);
        String tpl = TemplateHelper.render(spec, mData);
        if (HookSpec.EMAIL.equals(type)) {
            handleEmailHook(Mappers.YAML.deserialize(tpl, EmailHookSpec.class));
        } else if (HookSpec.NOTIFICATION.equals(type)) {
            handleNotificationHook(Mappers.YAML.deserialize(tpl, NotificationHook.class));
        } else {
            LOG.error("Hook type not supported: %s", type);
        }
    }


    private void handleEmailHook(EmailHookSpec model) {
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
    public HookSpec getHook(String operationId) {

        return HOOKS.computeIfAbsent(operationId, new Function<String, HookSpec>() {
            @SneakyThrows
            @Override
            public HookSpec apply(String s) {
                HookSpec hook = NO_HOOK;
                Resource res = rLoader.getResource("classpath:/hooks/" + operationId + ".yml");
                if (res.exists()) {
                    hook = Mappers.YAML.deserialize(res.getInputStream(), HookSpec.class);
                } else {
                    LOG.info("No hook registered for operation: %s", operationId);
                }
                return hook;
            }
        });
    }
}
