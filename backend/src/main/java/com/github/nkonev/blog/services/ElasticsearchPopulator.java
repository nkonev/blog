package com.github.nkonev.blog.services;

import com.github.nkonev.blog.Constants;
import com.github.nkonev.blog.entity.elasticsearch.IndexPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Value(Constants.CUSTOM_ELASTICSEARCH_DROP_FIRST)
    private boolean dropFirst;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value(Constants.ELASTICSEARCH_REFRESH_ON_START_KEY_TIMEOUT)
    private int timeout;

    @Value(Constants.ELASTICSEARCH_REFRESH_ON_START_KEY_TIMEUNIT)
    private TimeUnit timeUnit;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

    @PostConstruct
    public void pc()  {
        if (refreshOnStart) {
            LOGGER.info("Will try to refresh elasticsearch index");
            final String key = getKey();

            final boolean firstRun = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, LocalDateTime.now().format(formatter)));
            String dateTimeString = redisTemplate.opsForValue().get(key);
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

            if (dateTime.plus(Duration.of(timeout, timeUnit.toChronoUnit())).isBefore(LocalDateTime.now()) || firstRun) {
                LOGGER.info("Condition is successful, so we'll refresh elasticsearch index");
                postService.refreshFulltextIndex(false);
                redisTemplate.opsForValue().set(key, LocalDateTime.now().format(formatter));
                if (dropFirst){
                    redisTemplate.delete(key);
                }
            } else {
                LOGGER.info("Condition isn't successful, so we won't refresh elasticsearch index");
            }
        }
    }

    private String getKey() {
        return "elasticsearch:"+ IndexPost.INDEX+":build-time";
    }
}
