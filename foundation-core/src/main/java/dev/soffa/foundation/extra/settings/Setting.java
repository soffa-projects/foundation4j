package dev.soffa.foundation.extra.settings;

import dev.soffa.foundation.annotation.Store;
import dev.soffa.foundation.data.EntityModel;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Store("f_settings")
public class Setting implements EntityModel {

    public static final String ID_PREFIX = "st_";
    private String id;
    private String key;
    private String type;
    private String value;
}
