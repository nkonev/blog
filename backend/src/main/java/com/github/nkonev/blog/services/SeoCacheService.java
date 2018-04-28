package com.github.nkonev.blog.services;

import com.github.nkonev.blog.Constants;
import com.github.nkonev.blog.config.CustomConfig;
import com.github.nkonev.blog.config.PrerenderConfig;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import static com.github.nkonev.blog.Constants.Urls.POST;
import static com.github.nkonev.blog.utils.ServletUtils.nullToEmpty;

@Service
public class SeoCacheService {

    public static final String RENDERTRON_HTML = "rendertron_html_";

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

    private static final Logger LOGGER = LoggerFactory.getLogger(SeoCacheService.class);

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

    /**
     *
     * @param path "/", "/post/3"
     * @param query "", "?a=b&c=d"
     * @return
     */
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

    public void refreshPageCache(){
        LOGGER.info("Starting refreshing page cache");
        setHtml(getRedisKeyForIndex(), getRendrered(Constants.Urls.ROOT, ""));

        postRepository.findAll().forEach(post -> {
            setHtmlForPost(post.getId());
        });
        LOGGER.info("Finished refreshing page cache");
    }

    private void setHtmlForPost(Long postId) {
        if (postId == null) {return;}
        setHtml(getRedisKeyHtmlForPost(postId), getRendrered(Constants.Urls.POST + "/"+postId, ""));
    }

    private void setHtmlForIndex() {
        setHtml(getRedisKeyForIndex(), getRendrered("", ""));
    }

    public void rewriteCachedPost(Long id) {
        setHtmlForPost(id);
    }

    public void rewriteCachedIndex() {
        setHtmlForIndex();
    }

}
