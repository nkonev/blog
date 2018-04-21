package com.github.nkonev.blog.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider;
import net.javacrumbs.shedlock.spring.ScheduledLockConfiguration;
import net.javacrumbs.shedlock.spring.ScheduledLockConfigurationBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.time.Duration;

@ConditionalOnProperty(value = "custom.tasks.enable", matchIfMissing = true)
@Configuration
public class LockConfig {
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcLockProvider(dataSource, "locks.task_lock");
    }

    @Bean
    public ScheduledLockConfiguration taskScheduler(
            LockProvider lockProvider,
            @Value("${custom.tasks.poolSize}") int poolSize,
            @Value("${custom.tasks.defaultLockAtMostForSec}") int defaultLockAtMostForSec,
            @Value("${custom.tasks.defaultLockAtLeastForSec}") int defaultLockAtLeastForSec
            ) {
        return ScheduledLockConfigurationBuilder
                .withLockProvider(lockProvider)
                .withPoolSize(poolSize)
                .withDefaultLockAtMostFor(Duration.ofSeconds(defaultLockAtMostForSec))
                .withDefaultLockAtLeastFor(Duration.ofSeconds(defaultLockAtLeastForSec))
                .build();
    }
}
