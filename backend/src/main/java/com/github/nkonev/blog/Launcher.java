package com.github.nkonev.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Created by nik on 20.05.17.
 */
@EnableAsync
@SpringBootApplication(
        exclude = {SecurityAutoConfiguration.class, ThymeleafAutoConfiguration.class},
        scanBasePackages = {"com.github.nkonev.blog", "org.springframework.boot.actuate.metrics.export.prometheus"}
)
public class Launcher {


    public static void main(String[] args) throws Exception {
        SpringApplication.run(Launcher.class, args);
    }
}
