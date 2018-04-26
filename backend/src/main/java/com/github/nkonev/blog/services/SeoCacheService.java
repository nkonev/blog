package com.github.nkonev.blog.services;

import com.github.nkonev.blog.Constants;
import com.github.nkonev.blog.config.CustomConfig;
import com.github.nkonev.blog.config.PrerenderConfig;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import static com.github.nkonev.blog.Constants.Urls.POST;
import static com.github.nkonev.blog.utils.ServletUtils.nullToEmpty;

@Service
public class SeoCacheService {

    private static final String RENDERTRON_HTML = "rendertron_html_";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PrerenderConfig prerenderConfig;

    @Autowired
    private CustomConfig customConfig;

    private RestTemplate restTemplate;

    @Autowired
    private PostRepository postRepository;

    @PostConstruct
    public void pc() {
        restTemplate = new RestTemplate();
    }


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
     * @param path "/", "/poat/3"
     * @param query "", "?a=b&c=d"
     * @return
     */
    public String getRendrered(String path, String query){
        final String rendertronUrl = prerenderConfig.getPrerenderServiceUrl()
                + customConfig.getBaseUrl() + path + query;
        LOGGER.info("Requesting {} from rendertron", rendertronUrl);
        final ResponseEntity<String> re = restTemplate.getForEntity(rendertronUrl, String.class);
        return re.getBody();
    }

    public void refreshPageCache(){
        LOGGER.info("Starting refreshing page cache");
        setHtml(getRedisKeyForIndex(), getRendrered(Constants.Urls.ROOT, ""));

        postRepository.findAll().forEach(post -> {
            setHtml(getRedisKeyHtmlForPost(post.getId()), getRendrered(Constants.Urls.POST + "/"+post.getId(), ""));
        });
        LOGGER.info("Finished refreshing page cache");
    }
}
