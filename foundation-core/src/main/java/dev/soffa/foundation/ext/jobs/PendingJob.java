package dev.soffa.foundation.ext.jobs;

import dev.soffa.foundation.annotations.Store;
import dev.soffa.foundation.data.EntityModel;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Store("f_pending_jobs")
public class PendingJob implements EntityModel {

    private String id;
    private String operation;
    private String subject;
    private String data;
    private Date created;
    private String lastError;
    private int errorsCount;

    public void failed(String message) {
        lastError = message;
        errorsCount++;
    }


}
