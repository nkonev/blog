package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.services.AutocompleteService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by nik on 21.05.17.
 */
@RestController
@RequestMapping(Constants.Uls.API)
public class AutocompleteController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private AutocompleteService autocompleteService;

    @GetMapping("/q")
    public Set<String> q() throws IOException {
        return redisTemplate.opsForZSet().range(Constants.KEY, 0, -1);
    }

    @PostMapping(Constants.Uls.REPOPULATE)
    public void repopulate() throws IOException {
        autocompleteService.repopulate();
    }

    @ApiOperation(value = "autocomplete", nickname = Constants.Swagger.AUTOCOMPLETE, notes = "Autocomplete method which utilizes redis")
    @GetMapping(Constants.Uls.AUTOCOMPLETE)
    public List<String> autocomplete(String prefix) throws IOException {
        return autocompleteService.autocomplete(prefix, Constants.COUNT);
    }

}
