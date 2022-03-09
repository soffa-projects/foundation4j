package dev.soffa.foundation.spring.config.jobs;

/*
import dev.soffa.foundation.commons.IdGenerator;
import dev.soffa.foundation.multitenancy.TenantHolder;
import dev.soffa.foundation.pubsub.Message;
import dev.soffa.foundation.support.pubsub.MessageHandler;
import lombok.AllArgsConstructor;
import org.jobrunr.configuration.JobRunrConfiguration;
import org.jobrunr.jobs.lambdas.JobRequestHandler;


@AllArgsConstructor
public class JobManager implements JobRequestHandler<Job> {

    private MessageHandler handler;
    private JobRunrConfiguration.JobRunrConfigurationResult jobRunr;

    public Job enqueue(String description, Message event) {
        Job job = new Job(IdGenerator.secureRandomId("job_"), event.getContext().getTenantId(), description, event);
        jobRunr.getJobRequestScheduler().enqueue(job);
        return job;
    }

    @Override
    public void run(Job job) {
        TenantHolder.set(job.getTenant());
        handler.handle(job.getMessage());
    }

}
*/
