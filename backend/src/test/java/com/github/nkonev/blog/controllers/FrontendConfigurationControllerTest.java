package com.github.nkonev.blog.controllers;

import com.github.nkonev.blog.AbstractUtTestRunner;
import com.github.nkonev.blog.Constants;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FrontendConfigurationControllerTest extends AbstractUtTestRunner {

    @Test
    public void getConfig() throws Exception {
        mockMvc.perform(
                get(Constants.Uls.API+Constants.Uls.CONFIG)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleTemplate").value("%s | owner's blog"))
                .andExpect(jsonPath("$.header").value("Owner's blog"))
                .andReturn();

    }
}