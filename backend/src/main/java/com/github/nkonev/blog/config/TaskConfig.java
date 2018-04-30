package com.github.nkonev.blog.config;

import com.github.nkonev.blog.controllers.ImagePostContentUploadController;
import com.github.nkonev.blog.controllers.ImagePostTitleUploadController;
import com.github.nkonev.blog.controllers.ImageUserAvatarUploadController;
import com.github.nkonev.blog.services.SeoCacheService;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SchedulerLock;
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider;
import net.javacrumbs.shedlock.spring.ScheduledLockConfiguration;
import net.javacrumbs.shedlock.spring.ScheduledLockConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.concurrent.Executor;

@ConditionalOnProperty(value = "custom.tasks.enable", matchIfMissing = true)
@Configuration
public class TaskConfig {

    @Autowired
    private ImagePostContentUploadController imagePostContentUploadController;

    @Autowired
    private ImagePostTitleUploadController imagePostTitleUploadController;

    @Autowired
    private ImageUserAvatarUploadController imageUserAvatarUploadController;

    @Autowired
    private SeoCacheService seoCacheService;

    private static final String IMAGES_CLEAN_TASK = "imagesCleanTask";
    private static final String REFRESH_CACHE_CLEAN_TASK = "refreshCacheTask";

    public static final Logger LOGGER_IMAGE_CLEAN_TASK = LoggerFactory.getLogger(IMAGES_CLEAN_TASK);

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcLockProvider(dataSource, "locks.task_lock");
    }

    @Bean
    public ScheduledLockConfiguration taskScheduler(
            LockProvider lockProvider,
            @Value("${custom.tasks.poolSize}") int poolSize,
            @Value("${custom.tasks.defaultLockAtMostForSec}") int defaultLockAtMostForSec,
            @Value("${custom.tasks.defaultLockAtLeastForSec}") int defaultLockAtLeastForSec
            ) {
        return ScheduledLockConfigurationBuilder
                .withLockProvider(lockProvider)
                .withPoolSize(poolSize)
                .withDefaultLockAtMostFor(Duration.ofSeconds(defaultLockAtMostForSec))
                .withDefaultLockAtLeastFor(Duration.ofSeconds(defaultLockAtLeastForSec))
                .build();
    }

    @Bean(name = "taskExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(512);
        executor.setThreadNamePrefix("BlogAsync-");
        executor.initialize();
        return executor;
    }

    @Scheduled(cron = "${custom.tasks.images.clean.cron}")
    @SchedulerLock(name = IMAGES_CLEAN_TASK)
    public void cleanImages(){
        final int deletedPostContent = imagePostContentUploadController.clearPostContentImages();
        final int deletedPostTitles  = imagePostTitleUploadController.clearPostTitleImages();
        final int deletedAvatarImages = imageUserAvatarUploadController.clearAvatarImages();

        LOGGER_IMAGE_CLEAN_TASK.info("Cleared {} post content images(created before 1 day ago); {} post title images; {} user avatar images", deletedPostContent, deletedPostTitles, deletedAvatarImages);
    }


    @Scheduled(cron = "${custom.tasks.rendered.cache.refresh.cron}")
    @SchedulerLock(name = REFRESH_CACHE_CLEAN_TASK)
    public void refreshCache(){
        seoCacheService.refreshAllPagesCache();
    }

}
