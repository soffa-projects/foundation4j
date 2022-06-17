package dev.soffa.foundation.extra.jobs;

import dev.soffa.foundation.data.EntityRepository;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

public interface PendingJobRepository extends EntityRepository<PendingJob> {


    default void create(@NonNull String operation, @NonNull String subject) {
        insert(PendingJob.builder().operation(operation).subject(subject).build());
    }

    boolean isPending(String operation, String subject);

    void delete(String operation, String subjet);

    // boolean consume(String operation, String subjet);

    void consume(String operation, String subject, Runnable handler);

    void consume(String operation, Function<PendingJob, Boolean> consumer);

}
