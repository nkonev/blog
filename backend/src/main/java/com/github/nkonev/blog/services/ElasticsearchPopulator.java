package com.github.nkonev.blog.services;

import com.github.nkonev.blog.entity.elasticsearch.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import static com.github.nkonev.blog.config.ElasticsearchConfig.ELASTICSEARCH_CONFIG;
import static com.github.nkonev.blog.services.ElasticsearchPopulator.POPULATOR;

@Qualifier(POPULATOR)
@DependsOn(ELASTICSEARCH_CONFIG)
@Service
public class ElasticsearchPopulator {

    private static final Logger LOGGER  = LoggerFactory.getLogger(ElasticsearchPopulator.class);

    public static final String POPULATOR = "elasticsearchPopulator";

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
    public void pc() throws InterruptedException {
        TimeUnit.MINUTES.sleep(5);
        if (refreshOnStart) {
            LOGGER.info("Will try to refresh elasticsearch index");
            final String key = getKey();
            final boolean wasSet = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "true"));

            if (wasSet) {
                LOGGER.info("Probe is successful, so we'll refresh elasticsearch index");
                redisTemplate.expire(key, timeout, timeUnit);
                postService.refreshFulltextIndex();
                redisTemplate.delete(key);
                LOGGER.info("Successful delete probe");
            } else {
                LOGGER.info("Probe isn't successful, so we won't refresh elasticsearch index");
            }
        }
    }

    private String getKey() {
        return "elasticsearch:"+ Post.INDEX+":build";
    }

    public boolean refreshInProgress(){
        return redisTemplate.opsForValue().get(getKey()) != null;
    }

}
