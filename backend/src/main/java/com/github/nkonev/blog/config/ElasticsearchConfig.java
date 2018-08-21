package com.github.nkonev.blog.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.github.nkonev.blog.repo.elasticsearch")
@EntityScan(basePackages = "com.github.nkonev.blog.entity.elasticsearch")
public class ElasticsearchConfig {
}
