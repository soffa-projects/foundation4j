package dev.soffa.foundation.data.spring.features;

import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.data.SimpleRepository;
import dev.soffa.foundation.extra.jobs.PendingJob;
import dev.soffa.foundation.extra.jobs.PendingJobId;
import dev.soffa.foundation.extra.jobs.PendingJobRepo;
import dev.soffa.foundation.model.TenantId;
import org.springframework.stereotype.Component;

import java.util.function.Function;


@Component
public class PendingJobRepoImpl extends SimpleRepository<PendingJob, PendingJobId> implements PendingJobRepo {

    private static final Logger LOG = Logger.get(PendingJobRepo.class);
    private static final int RETRIES_TRESHOLD = 10;


    public PendingJobRepoImpl(DB db) {
        super(db, PendingJob.class, "f_pending_jobs", TenantId.DEFAULT_VALUE);
    }


    /*
    @Override
    public boolean consume(String operation, String subject) {
        PendingJob job = dev.soffa.foundation.get(ImmutableMap.of(OPERATION, operation, SUBJECT, subject)).orElse(null);
        if (job == null) {
            return false;
        }
        return delete(job) > 0;
    }*/

   /* @Override
    public void consume(PendingJobId id, Runnable handler) {
        PendingJob job = dev.soffa.foundation.get(ImmutableMap.of(OPERATION, operation, SUBJECT, subject)).orElse(null);
        if (job == null) {
            LOG.warn("No pending job found for operation: %s/%s", operation, subject);
            return;
        }
        try {
            handler.run();
            delete(job);
            LOG.info("Pending job consumed for operation: %s/%s", operation, subject);
        } catch (Exception e) {
            LOG.error("Error while handling pending job for operation: %s/ %s", operation, subject);
            LOG.error(e.getMessage());
            job.failed(e.getMessage());
            update(job);
            throw e;
        }

    }*/

    @Override
    public void consume(PendingJobId id, Function<PendingJob, Boolean> consumer) {
        PendingJob job = findById(TenantId.DEFAULT, id).orElse(null);
        if (job == null) {
            Logger.platform.warn("Pending job not found: %s", id);
            return;
        }
        try {
            if (consumer.apply(job)) {
                Logger.platform.info("Pending job [%s] consumed, removing from database", id);
                delete(TenantId.DEFAULT, job);
            }
        } catch (Exception e) {
            LOG.error(e);
            job.failed(e.getMessage());
            if (job.getErrorsCount() > RETRIES_TRESHOLD) {
                LOG.warn("Job %s has failed %d times !", job.getId(), job.getErrorsCount());
            }
            update(TenantId.DEFAULT, job);
        }
    }

}
