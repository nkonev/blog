package com.github.nkonev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by nik on 20.05.17.
 */

@SpringBootApplication(
        scanBasePackages = {"com.github.nkonev"}
)
public class ChatApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ChatApplication.class, args);
    }
}
