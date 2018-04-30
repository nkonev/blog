package com.github.nkonev.blog.services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Primary
@ConditionalOnProperty("${CUSTOM_HIBERNATE_LISTENER_ENABLE_ASYNC_CACHE_REFRESH}")
@Component
public class SeoCacheListenerAsyncProxy extends SeoCacheListenerSyncProxy implements SeoCacheListenerProxy {

    @Override
    @Async
    public void rewriteCachedIndex() {
        super.rewriteCachedIndex();
    }

    @Override
    @Async
    public void rewriteCachedPage(Long postId) {
        super.rewriteCachedPage(postId);
    }
}
