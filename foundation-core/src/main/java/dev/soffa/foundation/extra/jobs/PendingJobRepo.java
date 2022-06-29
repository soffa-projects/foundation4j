package dev.soffa.foundation.extra.jobs;

import dev.soffa.foundation.data.EntityRepository;

import java.util.function.Function;

public interface PendingJobRepo extends EntityRepository<PendingJob, PendingJobId> {

    void consume(PendingJobId id, Function<PendingJob, Boolean> consumer);

}
