package dev.soffa.foundation.model;

import lombok.Value;

@Value
public class OperationEntity<E> {

    Object eventId;
    E data;

}
