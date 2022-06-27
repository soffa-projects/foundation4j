package dev.soffa.foundation.data.jdbi;

import dev.soffa.foundation.commons.Mappers;
import dev.soffa.foundation.data.EntityInfo;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.ConvertUtils;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public final class BeanMapper<T> implements RowMapper<T> {

    private final EntityInfo<T> entityInfo;

    private BeanMapper(EntityInfo<T> entityInfo) {
        this.entityInfo = entityInfo;
    }

    public static <T> BeanMapper<T> of(EntityInfo<T> info) {
        return new BeanMapper<>(info);
    }

    @SneakyThrows
    @Override
    public T map(ResultSet rs, StatementContext ctx) {
        Map<String, Object> values = new HashMap<>();
        for (Map.Entry<String, String> e : entityInfo.getPropertiesToColumnsMapping().entrySet()) {
            String col = e.getValue();
            Object value = rs.getObject(col);
            String prop = e.getKey();
            Class<?> target = entityInfo.getPropertyType(prop);

            if (value == null) {
                values.put(col, null);
                continue;
            }

            value = ConvertUtils.convert(value, target);
            values.put(col, value);
            values.put(prop, value);

            /*

            if (value instanceof Clob) {
                value = rs.getString(col);
            }
            if (target.isInstance(value)) {
                values.put(col, value);
                continue;
            }
            boolean convertToMap = entityInfo.isCustomTypeOrMap(prop) && value instanceof String;
            if (convertToMap && Mappers.isJson(value.toString())) {
                if (Map.class.isAssignableFrom(target)) {
                    value = Mappers.JSON.deserializeMap(value.toString());
                } else {
                    value = Mappers.JSON_FULLACCESS_SNAKE.deserializeMap(value.toString());
                }
            }

             */
        }
        return Mappers.JSON_FULLACCESS_SNAKE.convert(values, entityInfo.getEntityClass());
    }

}
