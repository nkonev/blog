package com.github.nkonev.blog.services;


public interface SeoCacheService {
    String getHtmlFromCache(String key);

    void setHtml(String key, String value);

    void removeCachesForPost(Long postId);

    String getRendrered(String path, String query);

    void refreshPageCache();

    void rewriteCachedPost(Long id);

    void rewriteCachedIndex();
}
