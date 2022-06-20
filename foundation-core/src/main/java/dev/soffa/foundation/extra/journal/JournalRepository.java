package dev.soffa.foundation.extra.journal;

import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.data.EntityRepository;
import dev.soffa.foundation.error.ErrorUtil;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Supplier;

public interface JournalRepository extends EntityRepository<Journal> {

    default void log(Context context, @NonNull String kind, @NonNull Object subject, @NonNull String event) {
        insert(
            Journal.builder()
                .event(event)
                .subject(subject.toString())
                .kind(kind)
                .build()
                .withContext(context)
        );
    }

    @SneakyThrows
    default <T> T log(Context context, @NonNull String kind, @NonNull String subject, @NonNull String event, Supplier<T> supplier) {
        try {
            T result = supplier.get();
            log(context, kind, subject, event);
            return result;
        } catch (Exception e) {
            insert(
                Journal.builder()
                    .event(event)
                    .subject(subject)
                    .kind(kind)
                    .error(ErrorUtil.loookupOriginalMessage(e))
                    .status("failed")
                    .build()
                    .withContext(context)
            );
            throw e;
        }
    }


}
