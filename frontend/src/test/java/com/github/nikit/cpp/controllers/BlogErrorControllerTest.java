package com.github.nikit.cpp.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.nikit.cpp.AbstractUtTestRunner;
import com.github.nikit.cpp.Constants;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Map;

public class BlogErrorControllerTest extends AbstractUtTestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlogErrorControllerTest.class);

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

}
