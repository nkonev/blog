package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.services.AutocompleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by nik on 21.05.17.
 */
@RestController
public class AutocompleteController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private AutocompleteService autocompleteService;

    @RequestMapping("/q")
    public Set<String> home() throws IOException {
        return redisTemplate.opsForZSet().range(Constants.KEY, 0, -1);
    }

    @RequestMapping("/repopulate")
    public void repopulate() throws IOException {
        autocompleteService.repopulate();
    }

    @RequestMapping("/autocomplete")
    public List<String> autocomplete(String prefix) throws IOException {
        return autocompleteService.autocomplete(prefix, Constants.COUNT);
    }

}
