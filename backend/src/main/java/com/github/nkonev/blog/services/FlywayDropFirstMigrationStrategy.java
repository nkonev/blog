package com.github.nkonev.blog.services;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class FlywayDropFirstMigrationStrategy implements FlywayMigrationStrategy {

    @Value("${spring.flyway.drop-first:false}")
    private boolean dropFirst;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private FlywayProperties flywayProperties;

    private Logger LOGGER = LoggerFactory.getLogger(FlywayDropFirstMigrationStrategy.class);

    @Override
    public void migrate(Flyway flyway) {
        if (dropFirst){
            // flyway.clean();
            // TODO wait for Flywaydb 6.0 https://github.com/flyway/flyway/issues/2193

            for (String schema : flywayProperties.getSchemas()) {
                try (Connection connection = dataSource.getConnection();) {
                    try(Statement statement = connection.createStatement();){
                        statement.execute("drop schema \""+schema+"\" cascade; create schema \""+schema+"\";");
                        LOGGER.info("Schema \"{}\" successfully re-created", schema);
                    }
                } catch (SQLException e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.error(e.getMessage(), e);
                    } else {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        }
        flyway.migrate();
    }
}
