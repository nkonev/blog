package com.github.nkonev.blog.services;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.stereotype.Component;

@Component
public class CleanFlywayMigrationStrategy implements FlywayMigrationStrategy {

    @Value("${spring.flyway.drop-first:false}")
    private boolean dropFirst;

    @Override
    public void migrate(Flyway flyway) {
        if (dropFirst){
            flyway.clean();
        }
        flyway.migrate();
    }
}
