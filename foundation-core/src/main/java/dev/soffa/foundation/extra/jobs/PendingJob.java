package dev.soffa.foundation.extra.jobs;

import lombok.*;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingJob {

    private PendingJobId id;
    private String operation;
    private String subject;
    private String data;
    private Map<String, Object> metas;
    private Date created;
    private String lastError;
    private int errorsCount;

    public void failed(String message) {
        lastError = message;
        errorsCount++;
    }


}
