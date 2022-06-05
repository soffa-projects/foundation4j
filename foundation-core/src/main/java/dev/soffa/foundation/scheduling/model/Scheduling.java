package dev.soffa.foundation.scheduling.model;

import lombok.Data;

import java.util.List;

@Data
public class Scheduling {

    private boolean enabled;
    private List<Job> jobs;

    @Data
    public static class Job {
        private String action;
        private String cron;
    }

}
