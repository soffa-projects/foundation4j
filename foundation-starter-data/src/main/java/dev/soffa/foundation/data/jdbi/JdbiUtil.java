package dev.soffa.foundation.data.jdbi;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import javax.sql.DataSource;

public final class JdbiUtil {

    private JdbiUtil() {
    }

    public static void configure(Jdbi jdbi, boolean isPG) {
        //Jdbi jdbi = Jdbi.create(dataSource)
        jdbi.installPlugin(new SqlObjectPlugin());
        if (isPG) {
            jdbi.installPlugin(new PostgresPlugin());
        }
        jdbi.registerArgument(new SerializableArgumentFactory());
        jdbi.registerArgument(new Map00ArgumentFactory());
        jdbi.registerArgument(new Map01ArgumentFactory());
        jdbi.registerArgument(new Map02ArgumentFactory());
        jdbi.registerArgument(new Map03ArgumentFactory());
        jdbi.registerArgument(new Map04ArgumentFactory());
        jdbi.registerArgument(new List01ArgumentFactory());
        jdbi.registerArgument(new ObjectArgumentFactory());
    }

    public static Jdbi create(DataSource ds, boolean isPG) {
        Jdbi jdbi = Jdbi.create(ds);
        configure(jdbi, isPG);
        return jdbi;
    }


}


