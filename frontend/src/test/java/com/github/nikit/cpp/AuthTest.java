package com.github.nikit.cpp;

import com.github.nikit.cpp.config.SecurityConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthTest extends AbstractUtTestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthTest.class);

    @Test
    public void testAuth() throws Exception {
        // auth
        MvcResult loginResult = mockMvc.perform(
                post(SecurityConfig.API_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", username)
                        .param("password", password)
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andReturn();

        LOGGER.info(loginResult.getResponse().getContentAsString());
    }
}
