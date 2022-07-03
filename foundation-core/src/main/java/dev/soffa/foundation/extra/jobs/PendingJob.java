package dev.soffa.foundation.extra.jobs;

import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.context.Context;
import lombok.*;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

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

    public Optional<Context> getContext() {
        if (metas != null && metas.containsKey("context")) {
            return Optional.of(Mappers.JSON_DEFAULT.deserialize(metas.get("context").toString(), Context.class));
        }
        return Optional.empty();
    }


}
