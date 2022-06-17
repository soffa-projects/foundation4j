package dev.soffa.foundation.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceConfig {

    private String name;
    private String url;
    private String migration;
    private String tablesPrefix;

}
