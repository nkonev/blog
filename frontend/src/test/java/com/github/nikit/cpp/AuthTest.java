package com.github.nikit.cpp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.nikit.cpp.security.SecurityConfig;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static com.github.nikit.cpp.security.SecurityConfig.PASSWORD_PARAMETER;
import static com.github.nikit.cpp.security.SecurityConfig.USERNAME_PARAMETER;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthTest extends AbstractUtTestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthTest.class);

    @Test
    public void testAuth() throws Exception {
        // auth
        MvcResult loginResult = mockMvc.perform(
                post(SecurityConfig.API_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param(USERNAME_PARAMETER, username)
                        .param(PASSWORD_PARAMETER, password)
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andReturn();

        LOGGER.info(loginResult.getResponse().getContentAsString());
    }

    @Ignore
    @Test
    public void testNotAuthorized_() throws Exception {
        MvcResult result = mockMvc.perform(
                get(Constants.Uls.API+Constants.Uls.PROFILE)
        )
                .andExpect(status().isUnauthorized()) // 401
                .andReturn();

        String str = result.getResponse().getContentAsString();
        LOGGER.info(str);

        Map<String, String> resp = objectMapper.readValue(str, new TypeReference<Map<String, String>>(){});
        // check that Exception Handler hides Spring Security exceptions
        Assert.assertFalse(resp.containsKey("exception"));
        Assert.assertFalse(resp.containsValue("org.springframework.security.access.AccessDeniedException"));

        Assert.assertTrue(resp.containsKey("message"));
        Assert.assertEquals("Access is denied", resp.get("message"));
    }

    /**
     * We use restTemplate because Spring Security has own exception handling mechanism (not Spring MVC Exception Handler)
     * which eventually handled on Error Page
     * @throws Exception
     */
    @Test
    public void testNotAuthorized() throws Exception {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(urlWithContextPath()+Constants.Uls.API+Constants.Uls.PROFILE, String.class);
        String str = responseEntity.getBody();
        Assert.assertEquals(401, responseEntity.getStatusCodeValue());

        LOGGER.info(str);

        Map<String, String> resp = objectMapper.readValue(str, new TypeReference<Map<String, String>>(){});
        // check that Exception Handler hides Spring Security exceptions
        Assert.assertFalse(resp.containsKey("exception"));
        Assert.assertFalse(resp.containsKey("trace"));
        Assert.assertFalse(resp.containsValue("org.springframework.security.access.AccessDeniedException"));

        Assert.assertTrue(resp.containsKey("message"));
        Assert.assertEquals("Access is denied", resp.get("message"));
    }
}
