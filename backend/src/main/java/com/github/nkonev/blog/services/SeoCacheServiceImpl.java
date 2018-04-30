package com.github.nkonev.blog.services;

import com.github.nkonev.blog.Constants;
import com.github.nkonev.blog.config.CustomConfig;
import com.github.nkonev.blog.config.PrerenderConfig;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static com.github.nkonev.blog.Constants.CUSTOM_PRERENDER_ENABLE;
import static com.github.nkonev.blog.utils.SeoCacheKeyUtils.getRedisKeyForIndex;
import static com.github.nkonev.blog.utils.SeoCacheKeyUtils.getRedisKeyHtmlForPost;

@ConditionalOnProperty(CUSTOM_PRERENDER_ENABLE)
@Primary
@Service
public class SeoCacheServiceImpl implements SeoCacheService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PrerenderConfig prerenderConfig;

    @Autowired
    private CustomConfig customConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PostRepository postRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(SeoCacheServiceImpl.class);

    @Override
    public String getHtmlFromCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void setHtml(String key, String value){
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, prerenderConfig.getCacheExpire(), prerenderConfig.getCacheExpireTimeUnit());
    }

    @Override
    public void removeCachesForPost(Long postId) {
        if (postId != null){
            redisTemplate.delete(getRedisKeyHtmlForPost(postId));
            redisTemplate.delete(getRedisKeyForIndex());
        } else {
            redisTemplate.delete(getRedisKeyForIndex());
        }
    }


    /**
     *
     * @param path "/", "/post/3"
     * @param query "", "?a=b&c=d"
     * @return
     */
    @Override
    public String getRendrered(String path, String query){
        final String rendertronUrl = prerenderConfig.getPrerenderServiceUrl() + customConfig.getBaseUrl() + path + query;
        try {
            final RequestEntity<Void> requestEntity = RequestEntity.<Void>get(new URI(rendertronUrl))
                    .accept(MediaType.TEXT_HTML)
                    .build();
            LOGGER.info("Requesting {} from rendertron", rendertronUrl);
            final ResponseEntity<String> re = restTemplate.exchange(requestEntity, String.class);
            return re.getBody();

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void refreshPageCache(){
        LOGGER.info("Starting refreshing page cache");
        rewriteCachedIndex();

        postRepository.findPostIds().forEach(this::rewriteCachedPost);
        LOGGER.info("Finished refreshing page cache");
    }

    @Override
    public void rewriteCachedPost(Long postId) {
        if (postId == null) {return;}
        setHtml(getRedisKeyHtmlForPost(postId), getRendrered(Constants.Urls.POST + "/"+postId, ""));
    }

    @Override
    public void rewriteCachedIndex() {
        setHtml(getRedisKeyForIndex(), getRendrered("", ""));
    }
}
