package dev.soffa.foundation.data.migrations;

import dev.soffa.foundation.data.common.ExtDataSource;
import lombok.Data;

import java.util.function.Consumer;

@Data
public class MigrationJob {
    private ExtDataSource info;
    private Consumer<ExtDataSource> callback;


    public MigrationJob(ExtDataSource info) {
        this.info = info;
    }

    public MigrationJob(ExtDataSource info, Consumer<ExtDataSource> callback) {
        this.info = info;
        this.callback = callback;
    }
}
