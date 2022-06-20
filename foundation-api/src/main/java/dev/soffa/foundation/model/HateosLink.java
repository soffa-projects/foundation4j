package dev.soffa.foundation.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HateosLink {
    private String href;

    public HateosLink(String href) {
        this.href = href;
    }
}
