package com.github.nkonev.config;

import com.github.mongobee.Mongobee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@EnableReactiveMongoRepositories(basePackages = "com.github.nkonev.repo.mongodb")
@Configuration
public class MongoConfig {

    @Autowired
    private MongoProperties mongoProperties;

    @Bean
    public Mongobee mongobee(){
        final Mongobee runner = new Mongobee(mongoProperties.getUri());
        runner.setChangeLogsScanPackage("com.github.nkonev.mongo.changelogs"); // the package to be scanned for changesets
        return runner;
    }

}
