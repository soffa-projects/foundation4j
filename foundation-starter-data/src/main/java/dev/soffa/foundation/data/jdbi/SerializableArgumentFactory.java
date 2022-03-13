package dev.soffa.foundation.data.jdbi;

import dev.soffa.foundation.model.VO;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class SerializableArgumentFactory extends AbstractArgumentFactory<VO> {

    public SerializableArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(VO value, ConfigRegistry config) {
        return (position, statement, ctx) -> {
            statement.setString(position, value == null ? null : value.getValue());
        };
    }
}
