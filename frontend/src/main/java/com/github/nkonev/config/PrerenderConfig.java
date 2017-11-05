package com.github.nkonev.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties("custom")
public class PrerenderConfig {
    private final Map<String, String> prerender = new HashMap<>();

    public Map<String, String> getPrerender() {
        return prerender;
    }
}
