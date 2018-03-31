package com.github.nkonev.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationProperties("custom.prerender")
public class PrerenderConfig {
    private String crawlerUserAgents;
    private String forwardedURLPrefix;
    private String prerenderServiceUrl;
    private String ignoreExtensions;
    private TimeUnit cacheExpireTimeUnit = TimeUnit.MINUTES;
    private long cacheExpire = 30;

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

    public String getIgnoreExtensions() {
        return ignoreExtensions;
    }

    public void setIgnoreExtensions(String ignoreExtensions) {
        this.ignoreExtensions = ignoreExtensions;
    }

    public TimeUnit getCacheExpireTimeUnit() {
        return cacheExpireTimeUnit;
    }

    public void setCacheExpireTimeUnit(TimeUnit cacheExpireTimeUnit) {
        this.cacheExpireTimeUnit = cacheExpireTimeUnit;
    }

    public long getCacheExpire() {
        return cacheExpire;
    }

    public void setCacheExpire(long cacheExpire) {
        this.cacheExpire = cacheExpire;
    }
}
