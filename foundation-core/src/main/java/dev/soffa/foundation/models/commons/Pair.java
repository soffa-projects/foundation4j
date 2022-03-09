package dev.soffa.foundation.models.commons;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pair<A, B> {

    private A first;
    private B second;
}
