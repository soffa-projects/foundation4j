package dev.soffa.foundation.data.spring.features;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.data.SimpleEntityRepository;
import dev.soffa.foundation.extra.jobs.PendingJob;
import dev.soffa.foundation.extra.jobs.PendingJobRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;


@Component
public class PendingJobRepositoryImpl extends SimpleEntityRepository<PendingJob> implements PendingJobRepository {

    private static final Logger LOG = Logger.get(PendingJobRepository.class);
    private static final String OPERATION = "operation";
    private static final String SUBJECT = "subject";
    private static final int RETRIES_TRESHOLD = 10;


    public PendingJobRepositoryImpl(DB db) {
        super(db, PendingJob.class);
    }

    @Override
    public boolean isPending(String operation, String subject) {
        return count(ImmutableMap.of(OPERATION, operation, SUBJECT, subject)) > 0;
    }

    @Override
    public void delete(String operation, String subject) {
        delete(ImmutableMap.of(OPERATION, operation, SUBJECT, subject));
    }

    @Override
    public boolean consume(String operation, String subject) {
        PendingJob job = get(ImmutableMap.of(OPERATION, operation, SUBJECT, subject)).orElse(null);
        if (job == null) {
            return false;
        }
        return delete(job) > 0;
    }

    @Override
    public void consume(String operation, Function<PendingJob, Boolean> consumer) {
        List<PendingJob> jobs = find(ImmutableMap.of(OPERATION, operation));
        if (jobs == null || jobs.isEmpty()) {
            return;
        }
        for (PendingJob job : jobs) {
            try {
                if (consumer.apply(job)) {
                    delete(job);
                }
            } catch (Exception e) {
                LOG.error(e);
                job.failed(e.getMessage());
                if (job.getErrorsCount() > RETRIES_TRESHOLD) {
                    LOG.warn("Job %s has failed %d times !", job.getId(), job.getErrorsCount());
                }
                update(job);
            }
        }
    }

}
