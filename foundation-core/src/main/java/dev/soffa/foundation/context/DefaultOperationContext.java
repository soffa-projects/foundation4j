package dev.soffa.foundation.context;

import dev.soffa.foundation.activity.Activity;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.model.*;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
public class DefaultOperationContext implements OperationContext {

    private final Context internal;
    private final OperationSideEffects sideEffects = new OperationSideEffects();
    private transient final Logger logger;

    public DefaultOperationContext(@NonNull Context context, Class<?> operationClass) {
        this.internal = context;
        logger = Logger.getLogger(operationClass);
    }

    public static DefaultOperationContext create(Class<?> operationClass) {
        return new DefaultOperationContext(Context.create(), operationClass);
    }

    @Override
    public void hook(String name, String subject, Map<String, Object> data) {
        sideEffects.getHooks().add(new HookEntry(name, subject, data));
    }

    @Override
    public void hook(String name, Map<String, Object> data) {
        hook(name, UUID.randomUUID().toString(), data);
    }

    @Override
    public void event(String name, Map<String, Object> data) {
        sideEffects.getEvents().add(new Event(name, Mappers.JSON_DEFAULT.serialize(data)));
    }

    @Override
    public void dataPoint(@NonNull String metric, @NonNull Map<String, String> tags, @NonNull Map<String, Object> fields, @NonNull Date time) {
        sideEffects.getDataPoints().add(DataPoint.metric(metric).time(time).addTags(tags).addFields(fields));
    }

    @Override
    public void dataPoint(@NonNull String metric, @NonNull Map<String, String> tags, @NonNull Map<String, Object> fields) {
        dataPoint(metric, tags, fields, Date.from(Instant.now()));
    }

    @Override
    public void dataPoint(@NonNull DataPoint point) {
        sideEffects.getDataPoints().add(point);
    }

    @Override
    public void activity(@NonNull String event, String subject, Object data) {
        sideEffects.getActivities().add(new Activity(event, subject, Mappers.JSON_DEFAULT.serialize(data)));
    }

    //@Override
    //public <E, O, T extends Operation<E, O>> void delayed(String uuid, Class<T> operationClass, E input) {
    //    sideEffects.getDelayedJobs().add(new DelayedOperation<E>(uuid, operationClass.getSimpleName(), Serialized.of(input)));
    //}

    @Override
    public boolean isProduction() {
        return internal.isProduction();
    }

    @Override
    public boolean isLiveMode() {
        return internal.isLiveMode();
    }

    @Override
    public boolean hasAuthorization() {
        return internal.hasAuthorization();
    }

    @Override
    public String getSender() {
        return internal.getSender();
    }

    @Override
    public String getServiceName() {
        return internal.getServiceName();
    }

    @Override
    public TenantId getTenant() {
        return internal.getTenant();
    }

    @Override
    public String getTenantId() {
        return internal.getTenantId();
    }

    @Override
    public boolean hasTenant() {
        return internal.hasTenant();
    }

    @Override
    public boolean hasApplicationId() {
        return internal.hasApplicationId();
    }

    @Override
    public boolean hasAccountId() {
        return internal.hasAccountId();
    }

    @Override
    public boolean hasIpAddress() {
        return internal.hasIpAddress();
    }

    @Override
    public boolean isAuthenticated() {
        return internal.isAuthenticated();
    }

    @Override
    public Authentication getAuthentication() {
        return internal.getAuthentication();
    }

    @Override
    public Optional<String> getUsername() {
        return internal.getUsername();
    }

    @Override
    public Optional<String> getUserEmail() {
        return internal.getUserEmail();
    }

    @Override
    public String getAccountId() {
        return internal.getAccountId();
    }

    @Override
    public String getAccountName() {
        return internal.getAccountName();
    }

    @Override
    public String getTenantName() {
        return internal.getTenantName();
    }

    @Override
    public String getApplicationId() {
        return internal.getApplicationId();
    }

    @Override
    public String getIpAddress() {
        return internal.getIpAddress();
    }

    @Override
    public String getApplicationName() {
        return internal.getApplicationName();
    }
}
