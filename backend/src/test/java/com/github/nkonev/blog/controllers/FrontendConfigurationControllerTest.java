package com.github.nkonev.blog.controllers;

import com.github.nkonev.blog.AbstractUtTestRunner;
import com.github.nkonev.blog.Constants;
import org.junit.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FrontendConfigurationControllerTest extends AbstractUtTestRunner {

    @Test
    public void getConfig() throws Exception {
        mockMvc.perform(
                get(Constants.Urls.API+ Constants.Urls.CONFIG)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleTemplate").value("%s | nkonev's blog"))
                .andExpect(jsonPath("$.header").value("Блог Конева Никиты"))
                .andExpect(jsonPath("$.showSettings").value(false))
                .andExpect(jsonPath("$.removeImageBackground").doesNotExist())
                .andReturn();

    }
}