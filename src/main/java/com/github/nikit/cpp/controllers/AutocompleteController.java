package com.github.nikit.cpp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by nik on 21.05.17.
 */
@RestController
public class AutocompleteController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @RequestMapping("/q")
    public String home(@RequestParam("q") String q) {
        return redisTemplate.opsForValue().get(q);
    }

}
