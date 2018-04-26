package com.github.nkonev.blog.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.nkonev.blog.AbstractUtTestRunner;
import com.github.nkonev.blog.Constants;
import com.github.nkonev.blog.TestConstants;
import com.github.nkonev.blog.security.SecurityConfig;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MvcResult;
import java.net.URI;
import java.util.Map;

import static com.github.nkonev.blog.security.SecurityConfig.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BlogErrorControllerTest extends AbstractUtTestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlogErrorControllerTest.class);

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

    /**
     * We use restTemplate because Spring Security has own exception handling mechanism (not Spring MVC Exception Handler)
     * which eventually handled on Error Page
     * @throws Exception
     */
    @Test
    public void testNotAuthorized() throws Exception {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(urlWithContextPath()+ Constants.Urls.API+ Constants.Urls.PROFILE, String.class);
        String str = responseEntity.getBody();
        Assert.assertEquals(401, responseEntity.getStatusCodeValue());

        LOGGER.info(str);

        Map<String, Object> resp = objectMapper.readValue(str, new TypeReference<Map<String, Object>>(){});
        // check that Exception Handler hides Spring Security exceptions
        Assert.assertFalse(resp.containsKey("exception"));
        Assert.assertFalse(resp.containsKey("trace"));
        Assert.assertFalse(resp.containsValue("org.springframework.security.access.AccessDeniedException"));

        Assert.assertTrue(resp.containsKey("message"));
        Assert.assertNotNull(resp.get("message"));
    }

    @Test
    public void testNotFoundJs() throws Exception {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(urlWithContextPath()+"/not-exists", String.class);
        String str = responseEntity.getBody();
        Assert.assertEquals(404, responseEntity.getStatusCodeValue());

        LOGGER.info(str);

        Map<String, Object> resp = objectMapper.readValue(str, new TypeReference<Map<String, Object>>(){});

        Assert.assertTrue(responseEntity.getHeaders().getContentType().toString().contains(MediaType.APPLICATION_JSON_UTF8_VALUE));
        Assert.assertEquals("Not Found", resp.get("error"));
        Assert.assertEquals(404, resp.get("status"));
    }

    @Test
    public void test404Fallback() throws Exception {
        RequestEntity<Void> requestEntity = RequestEntity.<Void>get(new URI(urlWithContextPath()+"/not-exists")).accept(MediaType.TEXT_HTML).build();

        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        String str = responseEntity.getBody();
        Assert.assertEquals(200, responseEntity.getStatusCodeValue()); // we respond 200 for 404 fallback

        LOGGER.info(str);
        LOGGER.info("HTML 404 fallback Content-Type: {}", responseEntity.getHeaders().getContentType());
        Assert.assertTrue(responseEntity.getHeaders().getContentType().toString().contains(MediaType.TEXT_HTML_VALUE));
        Assert.assertTrue(str.contains("<!doctype html>"));
    }

    @Test
    public void testSqlExceptionIsHidden() throws Exception {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(urlWithContextPath()+ Constants.Urls.API+ TestConstants.SQL_URL, String.class);
        String str = responseEntity.getBody();
        Assert.assertEquals(500, responseEntity.getStatusCodeValue());

        LOGGER.info(str);

        Map<String, Object> resp = objectMapper.readValue(str, new TypeReference<Map<String, Object>>(){});
        Assert.assertFalse(resp.containsKey("exception"));
        Assert.assertFalse(resp.containsKey("trace"));
        Assert.assertFalse(resp.containsValue(TestConstants.SQL_QUERY));

        Assert.assertEquals("internal error", resp.get("message"));
        Assert.assertEquals("Internal Server Error", resp.get("error"));
    }

    @Test
    public void testUserDetailsWithPasswordIsNotSerialized() throws Exception {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(urlWithContextPath()+ Constants.Urls.API+TestConstants.USER_DETAILS, String.class);
        String str = responseEntity.getBody();
        Assert.assertEquals(500, responseEntity.getStatusCodeValue());

        LOGGER.info(str);

        Map<String, Object> resp = objectMapper.readValue(str, new TypeReference<Map<String, Object>>(){});
        Assert.assertFalse(resp.containsKey("exception"));
        Assert.assertFalse(resp.containsKey("trace"));

        Assert.assertEquals("internal error", resp.get("message"));
        Assert.assertEquals("Internal Server Error", resp.get("error"));
    }
}
