package dev.soffa.foundation.data.jdbi;

import dev.soffa.foundation.commons.Mappers;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;
import java.util.ArrayList;

public class List01ArgumentFactory extends AbstractArgumentFactory<ArrayList<Object>> {

    public List01ArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(ArrayList<Object> value, ConfigRegistry config) {
        return (position, statement, ctx) -> {
            statement.setString(position, value == null ? null : Mappers.JSON_DEFAULT.serialize(value));
        };
    }
}
