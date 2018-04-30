package com.github.nkonev.blog.services;


import javax.servlet.http.HttpServletRequest;

public interface SeoCacheService {
    String getHtmlFromCache(String key);

    void removeAllPagesCache(Long postId);

    void refreshAllPagesCache();

    void rewriteCachedPost(Long id);

    void rewriteCachedIndex();

    String rewriteCachedPost(HttpServletRequest request);
}
