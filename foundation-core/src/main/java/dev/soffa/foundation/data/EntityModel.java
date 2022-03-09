package dev.soffa.foundation.data;

import java.util.Date;

public interface EntityModel extends EntityLifecycle {

    String getId();

    void setId(String value);

    default Date getCreated() {
        return null;
    }

    default void setCreated(Date date) {
        // Default implementation
    }
}
