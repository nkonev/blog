package com.github.nkonev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;

/**
 * Created by nik on 20.05.17.
 */

@SpringBootApplication(
        exclude = {SecurityAutoConfiguration.class, ThymeleafAutoConfiguration.class},
        scanBasePackages = {"com.github.nkonev", "org.springframework.boot.actuate.metrics.export.prometheus"}
)
public class Launcher {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Launcher.class, args);
    }
}
