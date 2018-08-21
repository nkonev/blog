package com.github.nkonev.blog.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan(basePackages = "com.github.nkonev.blog.entity.jpa")
@EnableJpaRepositories(basePackages = "com.github.nkonev.blog.repo.jpa")
@EnableTransactionManagement
public class DbConfig {

}
