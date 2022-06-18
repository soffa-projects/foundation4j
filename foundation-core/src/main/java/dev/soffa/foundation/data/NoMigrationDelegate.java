package dev.soffa.foundation.data;

public class NoMigrationDelegate implements MigrationDelegate {

    @Override
    public String getMigrationName(String tenant) {
        return "auto";
    }
}
