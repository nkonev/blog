package com.github.nkonev.blog.controllers;

import com.github.nkonev.blog.dto.FrontendConfigurationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.github.nkonev.blog.Constants.Urls.*;

@RestController
public class FrontendConfigurationController {
    @Autowired
    private FrontendConfigurationDTO frontendConfigurationDTO;

    @GetMapping(API+CONFIG)
    public FrontendConfigurationDTO getConfig(){
        return frontendConfigurationDTO;
    }
}
