package dev.soffa.foundation.core.model;

import dev.soffa.foundation.commons.Mappers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Serialized implements Serializable {

    private static final long serialVersionUID = 1L;
    private String type;
    private String data;

    public static Serialized of(Object input) {
        if (input instanceof Serialized) {
            return (Serialized)input;
        }
        if (input.getClass() == Object.class) {
            throw new IllegalArgumentException("Cannot serialize object of type " + input.getClass());
        }
        return new Serialized(input.getClass().getName(), Mappers.JSON_DEFAULT.serialize(input));
    }
}
