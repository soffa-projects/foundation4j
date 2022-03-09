package dev.soffa.foundation.data.spring;

import dev.soffa.foundation.data.DataSourceConfig;
import lombok.Getter;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Objects;

@Getter
public class DatasourceInfo {

    private final String name;
    private final DataSourceConfig config;
    private DataSource dataSource;
    private boolean migrated;
    private LocalContainerEntityManagerFactoryBean em;
    private PlatformTransactionManager tx;

    public DatasourceInfo(String name, DataSourceConfig config) {
        this.config = config;
        this.name = name.toLowerCase();
    }

    public DatasourceInfo(String name, DataSourceConfig config, DataSource dataSource) {
        this(name, config);
        this.dataSource = dataSource;

    }

    public void configureTx(EntityManagerFactoryBuilder builder, String... packages) {
        this.em = builder.dataSource(dataSource).packages(packages)
            .persistenceUnit(name)
            .build();
        this.tx = new JpaTransactionManager(Objects.requireNonNull(this.em.getObject()));
    }


    public void setMigrated(boolean value) {
        this.migrated = value;
    }
}
