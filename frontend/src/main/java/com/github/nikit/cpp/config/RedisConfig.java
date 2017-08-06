package com.github.nikit.cpp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Configuration
@EnableRedisRepositories(basePackages="com.github.nikit.cpp.repo.redis")
public class RedisConfig {

}
