package com.github.nikit.cpp.config;

import org.springframework.http.MediaType;

import java.util.List;

public interface ImageConfig {
    List<MediaType> getAllowedMimeTypes();

    public long getMaxBytes();
}
