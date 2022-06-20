package dev.soffa.foundation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.models.links.Link;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class RepresentationModel {

    @JsonProperty(value = "_links", access = JsonProperty.Access.READ_ONLY)
    private transient Link links;

}
