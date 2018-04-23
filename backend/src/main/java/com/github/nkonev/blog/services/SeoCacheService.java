package com.github.nkonev.blog.services;

import com.github.nkonev.blog.config.PrerenderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

import static com.github.nkonev.blog.Constants.Uls.POST;
import static com.github.nkonev.blog.utils.ServletUtils.nullToEmpty;

@Service
public class SeoCacheService {

    private static final String RENDERTRON_HTML = "rendertron_html_";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PrerenderConfig prerenderConfig;

    public String getHtmlFromCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void setHtml(String key, String value){
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, prerenderConfig.getCacheExpire(), prerenderConfig.getCacheExpireTimeUnit());
    }

    public void removeCachesForPost(Long postId) {
        if (postId != null){
            redisTemplate.delete(getRedisKeyHtmlForPost(postId));
            redisTemplate.delete(getRedisKeyForIndex());
        } else {
            redisTemplate.delete(getRedisKeyForIndex());
        }
    }


    public static String getRedisKeyHtml(HttpServletRequest clientRequest) {
        return RENDERTRON_HTML + clientRequest.getRequestURI() + nullToEmpty(clientRequest.getQueryString());
    }

    public static String getRedisKeyHtmlForPost(Long postId) {
        return RENDERTRON_HTML + POST + "/" + postId;
    }

    public static String getRedisKeyForIndex(){
        return RENDERTRON_HTML + "/";
    }
}
