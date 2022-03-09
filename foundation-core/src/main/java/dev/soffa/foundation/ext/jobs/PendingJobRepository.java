package dev.soffa.foundation.ext.jobs;

import dev.soffa.foundation.data.EntityRepository;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

public interface PendingJobRepository extends EntityRepository<PendingJob> {


    default void create(@NonNull String operation, @NonNull String subject) {
        insert(PendingJob.builder().operation(operation).subject(subject).build());
    }

    boolean isPending(String operation, String subject);

    void delete(String operation, String subbjet);

    boolean consume(String operation, String subbjet);

    void consume(String operation, Function<PendingJob, Boolean> consumer);

}
