package com.github.nikit.cpp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;

/**
 * Created by nik on 20.05.17.
 */

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, ThymeleafAutoConfiguration.class})
public class Launcher {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Launcher.class, args);
    }
}
