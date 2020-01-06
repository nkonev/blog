package com.github.nkonev.blog.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan(basePackages = "com.github.nkonev.blog.entity.jdbc")
@EnableJdbcRepositories(basePackages = "com.github.nkonev.blog.repo.jdbc")
@EnableTransactionManagement
public class DbConfig {

}
