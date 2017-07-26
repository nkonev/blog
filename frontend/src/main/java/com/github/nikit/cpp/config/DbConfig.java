package com.github.nikit.cpp.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan(basePackages = "com.github.nikit.cpp.entity")
@EnableJpaRepositories(basePackages = "com.github.nikit.cpp.repo")
@EnableTransactionManagement
public class DbConfig {
}
