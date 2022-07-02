package dev.soffa.foundation.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceConfig {

    private String name;
    private String tablesPrefix;
    private String url;
    private String migration;

    public DataSourceConfig(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public DataSourceConfig(String name, String url, String migration) {
        this.name = name;
        this.url = url;
        this.migration = migration;
    }


}
