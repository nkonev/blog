package com.github.nkonev.blog.services;

import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DbCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbCleaner.class);

    private static final int TIME_MS = 20 * 1000;

    @Scheduled(cron = "*/20 * * * * *")
    @SchedulerLock(name = "scheduledTaskName", lockAtMostFor = TIME_MS, lockAtLeastFor = TIME_MS)
    public void clean(){
        LOGGER.info("tick");
    }
}
