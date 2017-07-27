package com.github.nikit.cpp.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EntityScan(basePackages = "com.github.nikit.cpp.entity")
@EnableJpaRepositories(basePackages = "com.github.nikit.cpp.repo")
@EnableTransactionManagement
public class DbConfig {

    // https://docs.spring.io/spring-boot/docs/current/reference/html/howto-data-access.html#howto-two-datasources
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties fooDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return fooDataSourceProperties().initializeDataSourceBuilder().build();
    }

    public static final String AUTH_DATASOURCE_BEAN_NAME = "authDatasource";

    @Bean(name = AUTH_DATASOURCE_BEAN_NAME)
    @ConfigurationProperties("auth.datasource")
    public DataSource authDataSource() {
        return DataSourceBuilder.create().build();
    }
}
