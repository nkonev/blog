package com.github.nkonev.blog.config;

import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession(redisNamespace = "blog-session")
public class SessionConfig {
    // customization example https://github.com/thomasdarimont/spring-session-example/blob/master/src/main/java/de/tdlabs/training/demo/CustomRedisSessionConfig.java
}
