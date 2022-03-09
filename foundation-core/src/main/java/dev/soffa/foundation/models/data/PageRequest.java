package dev.soffa.foundation.models.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    private int number;
    private int size = 10;

}
