package com.github.nikit.cpp.services;

import com.github.nikit.cpp.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nik on 23.05.17.
 */
@Service
public class AutocompleteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutocompleteService.class);

    @Value("classpath:/countries.txt")
    private org.springframework.core.io.Resource resource;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private List<String> resourceToString() throws IOException {
        InputStream object = resource.getInputStream();
        try(InputStreamReader isr = new InputStreamReader(object, StandardCharsets.UTF_8)){
            try(BufferedReader bufferedReader = new BufferedReader(isr)){
                return bufferedReader.lines().collect(Collectors.toList());
            }
        }
    }

    public void repopulate() throws IOException {
        redisTemplate.opsForZSet().removeRange(Constants.KEY, 0, -1);
        List<String> list = resourceToString();
        for(String line: list) {
            for (int l = 0; l<=line.length(); ++l){
                String prefix = line.substring(0, l);
                redisTemplate.opsForZSet().add(Constants.KEY, prefix, 0);
            }
            redisTemplate.opsForZSet().add(Constants.KEY, line+ Constants.STOP_SYMBOL, 0);
        }
        // LOGGER.info("{}", list);
    }
}
