package com.github.nkonev.blog.services;

import com.github.nkonev.blog.entity.elasticsearch.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Service
public class ElasticsearchPopulator {

    private static final Logger LOGGER  = LoggerFactory.getLogger(ElasticsearchPopulator.class);

    @Autowired
    private PostService postService;

    @Value("${custom.elasticsearch.refresh-on-start:true}")
    private boolean refreshOnStart;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${custom.elasticsearch.refresh-on-start-key.timeout:10}")
    private int timeout;

    @Value("${custom.elasticsearch.refresh-on-start-key.timeunit:MINUTES}")
    private TimeUnit timeUnit;

    @PostConstruct
    public void pc(){

        if (refreshOnStart) {
            final String key = "elasticsearch:"+ Post.INDEX+":build";
            final boolean wasSet = redisTemplate.opsForValue().setIfAbsent(key, "true");

            if (wasSet) {
                redisTemplate.expire(key, timeout, timeUnit);
                postService.refreshFulltextIndex();
                redisTemplate.delete(key);
            }
        }
    }

}
