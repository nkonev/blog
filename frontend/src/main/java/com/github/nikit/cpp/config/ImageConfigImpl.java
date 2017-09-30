package com.github.nikit.cpp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix="custom.image")
public class ImageConfigImpl implements ImageConfig {

    private List<MediaType> allowedMimeTypes;

    private long maxBytes;

    public List<MediaType> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(List<MediaType> allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }

    public long getMaxBytes() {
        return maxBytes;
    }

    public void setMaxBytes(long maxBytes) {
        this.maxBytes = maxBytes;
    }
}
