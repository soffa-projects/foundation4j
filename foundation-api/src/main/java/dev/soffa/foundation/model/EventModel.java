package dev.soffa.foundation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface EventModel {

    @JsonIgnore
    String getId();
}
