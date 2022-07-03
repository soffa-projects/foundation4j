package dev.soffa.foundation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.Mappers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectFactoryTest {

    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";
    private static final String VALUE_3 = "value3";
    private static final String VALUE_4 = "value4";

    @Test
    public void testObjectFactory() {
        Model m = new Model(VALUE_1, VALUE_2, VALUE_3, VALUE_4);

        String serialized = Mappers.JSON_DEFAULT.serialize(m);

        assertFalse(serialized.contains(VALUE_1));
        assertTrue(serialized.contains(VALUE_2));
        assertTrue(serialized.contains(VALUE_3));
        assertFalse(serialized.contains(VALUE_4));

        Model m2 = Mappers.JSON_DEFAULT.deserialize(serialized, Model.class);
        assertNull(m2.getProp1());
        assertEquals(m.getProp2(), m2.getProp2());
        assertNull(m2.getProp3());

        serialized = Mappers.JSON_FULLACCESS.serialize(m);

        assertTrue(serialized.contains(VALUE_1));
        assertTrue(serialized.contains(VALUE_2));
        assertTrue(serialized.contains(VALUE_3));
        assertTrue(serialized.contains(VALUE_4));

        Model m3 = Mappers.JSON_FULLACCESS.deserialize(serialized, Model.class);
        assertEquals(m.getProp1(), m3.getProp1());
        assertEquals(m.getProp2(), m3.getProp2());
        assertEquals(m.getProp3(), m3.getProp3());
        assertEquals(m.getLastProp(), m3.getLastProp());

        Map<String, Object> data = ImmutableMap.of(
            "prop1", VALUE_1,
            "prop2", VALUE_2,
            "prop3", VALUE_3,
            "lastProp", VALUE_4
        );

        Model m4 = Mappers.JSON_FULLACCESS.convert(data, Model.class);
        assertEquals(m.getProp1(), m4.getProp1());
        assertEquals(m.getProp2(), m4.getProp2());
        assertEquals(m.getProp3(), m4.getProp3());
        assertEquals(m.getLastProp(), m4.getLastProp());

        data = ImmutableMap.of(
            "prop1", VALUE_1,
            "prop2", VALUE_2,
            "prop3", VALUE_3,
            "last_prop", VALUE_4
        );

        Model m5 = Mappers.JSON_FULLACCESS_SNAKE.convert(data, Model.class);
        assertEquals(m.getProp1(), m5.getProp1());
        assertEquals(m.getProp2(), m5.getProp2());
        assertEquals(m.getProp3(), m5.getProp3());
        assertEquals(m.getLastProp(), m5.getLastProp());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Model {
        @JsonIgnore
        private String prop1;
        private String prop2;
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private String prop3;
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private String lastProp;
    }
}
