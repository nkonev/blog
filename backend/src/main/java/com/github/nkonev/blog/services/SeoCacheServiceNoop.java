package com.github.nkonev.blog.services;

import org.springframework.stereotype.Service;

@Service
public class SeoCacheServiceNoop implements SeoCacheService {
    @Override
    public String getHtmlFromCache(String key) {
        return null;
    }

    @Override
    public void setHtml(String key, String value) {

    }

    @Override
    public void removeCachesForPost(Long postId) {

    }

    @Override
    public String getRendrered(String path, String query) {
        return null;
    }

    @Override
    public void refreshPageCache() {

    }

    @Override
    public void setHtmlForPost(Long postId) {

    }

    @Override
    public void setHtmlForIndex() {

    }

    @Override
    public void rewriteCachedPost(Long id) {

    }

    @Override
    public void rewriteCachedIndex() {

    }
}
