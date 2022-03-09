package dev.soffa.foundation.data.spring;

import dev.soffa.foundation.config.AppConfig;
import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.multitenancy.TenantsLoader;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;

@Configuration
public class DBConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TenantsLoader createDefaultTenantsLoader() {
        return TenantsLoader.NOOP;
    }

    @Bean
    public DB createDB(AppConfig appConfig, ApplicationContext context) {
        appConfig.configure();
        return new DBImpl(context, appConfig);
    }

    @Bean
    @Primary
    public DataSource createDatasource(DB db) {
        return (DataSource) db;
    }

    @Bean
    @ConditionalOnBean(DataSource.class)
    public Jdbi jdbi(DB datasource) {
        return Jdbi.create(new TransactionAwareDataSourceProxy((DataSource) datasource))
            //.installPlugin(new PostgresPlugin())
            .installPlugin(new SqlObjectPlugin());
    }


}
