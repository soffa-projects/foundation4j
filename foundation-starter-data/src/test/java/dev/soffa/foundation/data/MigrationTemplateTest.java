package dev.soffa.foundation.data;

import dev.soffa.foundation.data.migrations.MigrationTemplateHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MigrationTemplateTest {

    @Test
    public void testMigrationTemplate() {
        String lb = MigrationTemplateHelper.transform(MigrationTemplateTest.class.getResourceAsStream("/db/changelog/foundation/journal.yml"));
        assertNotNull(lb);
        assertFalse(lb.isEmpty());
    }
}
