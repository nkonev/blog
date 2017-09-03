package com.github.nikit.cpp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Configuration
// https://stackoverflow.com/questions/41693774/spring-redis-indexes-not-deleted-after-main-entry-expires/41695902#41695902
@EnableRedisRepositories(basePackages="com.github.nikit.cpp.repo.redis", enableKeyspaceEvents=RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
public class RedisConfig {

}
