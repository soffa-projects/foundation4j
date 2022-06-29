package dev.soffa.foundation.extra.jobs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessSideEffectInput implements Serializable {

    private static final long serialVersionUID = 1L;

    private PendingJobId id;

}
