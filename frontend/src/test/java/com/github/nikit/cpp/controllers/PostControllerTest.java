package com.github.nikit.cpp.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nikit.cpp.AbstractUtTestRunner;
import com.github.nikit.cpp.Constants;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostControllerTest extends AbstractUtTestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostControllerTest.class);

    @Autowired
    private PostController postController;

    @Autowired
    private ObjectMapper objectMapper;

    public static class PostDtoBuilder {
        public static class Instance {
            private final PostController.PostDTO postDTO;
            {
                try {
                    postDTO = new PostController.PostDTO(
                            0,
                            "default new post title",
                            "default new post text",
                            new URL("https://postgrespro.ru/img/logo_mono.png")
                    );
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            public PostController.PostDTO build() {
                return postDTO;
            }
        }

        public static Instance startBuilding() {
            return new Instance();
        }
    }

    @WithUserDetails("alice")
    @Test
    public void testUserCanAddPost() throws Exception {
        MvcResult addPostRequest = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.POST)
                        .content(objectMapper.writeValueAsString(PostDtoBuilder.startBuilding().build()))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andReturn();
        LOGGER.info(addPostRequest.getResponse().getContentAsString());

        // check post present in my posts
        MvcResult getMyPostsRequest = mockMvc.perform(
                get(Constants.Uls.API+Constants.Uls.POST+Constants.Uls.MY)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andReturn();
        String strListPosts = getMyPostsRequest.getResponse().getContentAsString();
        LOGGER.info(strListPosts);
        List<PostController.PostDTO> posts = objectMapper.readValue(strListPosts, new TypeReference<List<PostController.PostDTO>>(){});
        Assert.assertTrue("I should can see my created post",
                posts.stream().anyMatch(postDTO -> postDTO.getTitle().equals("default new post title")));
        // check foreign post not present in my posts
        Assert.assertFalse("foreign post shouldn't be in my posts",
                posts.stream().anyMatch(postDTO -> postDTO.getTitle().startsWith("generated_post")));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testAnonymousCannotAddPostUnit() throws Exception {
        postController.createPost(null, PostDtoBuilder.startBuilding().build());
    }

    @Test
    public void testAnonymousCannotAddPost() throws Exception {
        MvcResult addPostRequest = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.POST)
                        .content(objectMapper.writeValueAsString(PostDtoBuilder.startBuilding().build()))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isUnauthorized())
                .andReturn();
        LOGGER.info(addPostRequest.getResponse().getContentAsString());
    }



//    @Test
//    public void testUserCanUpdateHisPost() throws Exception {
//
//    }
//
//    @Test
//    public void testUserCannotUpdateForeignPost() throws Exception {
//
//    }


}
