package com.github.nkonev.blog;

import com.github.nkonev.blog.config.ApplicationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Created by nik on 20.05.17.
 */
@EnableAsync
@SpringBootApplication(
        scanBasePackages = {"com.github.nkonev.blog"},
        exclude = {RestClientAutoConfiguration.class}
)
@EnableConfigurationProperties({ApplicationConfig.class})
public class Launcher {

    static {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        System.setProperty("hibernate.types.print.banner", "false");
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Launcher.class, args);
    }
}
