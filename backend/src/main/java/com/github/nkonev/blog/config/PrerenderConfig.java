package com.github.nkonev.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties("custom.prerender")
public class PrerenderConfig {
    private String crawlerUserAgents;
    private String forwardedURLPrefix;
    private String prerenderServiceUrl;

    public PrerenderConfig() { }

    public String getCrawlerUserAgents() {
        return crawlerUserAgents;
    }

    public void setCrawlerUserAgents(String crawlerUserAgents) {
        this.crawlerUserAgents = crawlerUserAgents;
    }

    public String getForwardedURLPrefix() {
        return forwardedURLPrefix;
    }

    public void setForwardedURLPrefix(String forwardedURLPrefix) {
        this.forwardedURLPrefix = forwardedURLPrefix;
    }

    public String getPrerenderServiceUrl() {
        return prerenderServiceUrl;
    }

    public void setPrerenderServiceUrl(String prerenderServiceUrl) {
        this.prerenderServiceUrl = prerenderServiceUrl;
    }

    @Override
    public String toString() {
        return "PrerenderConfig{" +
                "crawlerUserAgents='" + crawlerUserAgents + '\'' +
                ", forwardedURLPrefix='" + forwardedURLPrefix + '\'' +
                ", prerenderServiceUrl='" + prerenderServiceUrl + '\'' +
                '}';
    }
}
