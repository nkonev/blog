package com.github.nikit.cpp.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nikit.cpp.AbstractUtTestRunner;
import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.dto.PostDTO;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostControllerTest extends AbstractUtTestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostControllerTest.class);

    @Autowired
    private PostController postController;

    @Autowired
    private ObjectMapper objectMapper;

    public static final String USER_ALICE = "alice";
    public static final String USER_ADMIN  ="admin";

    public static class PostDtoBuilder {
        public static class Instance {
            private final PostDTO postDTO;
            {
                try {
                    postDTO = new PostDTO(
                            0,
                            "default new post title",
                            "default new post text",
                            new URL("https://postgrespro.ru/img/logo_mono.png")
                    );
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            public PostDTO build() {
                return postDTO;
            }

            public Instance id(long id) {
                postDTO.setId(id);
                return this;
            }
        }

        public static Instance startBuilding() {
            return new Instance();
        }
    }

    @WithUserDetails(USER_ALICE)
    @Test
    public void testUserCanAddAndUpdatePost() throws Exception {
        MvcResult addPostRequest = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.POST)
                        .content(objectMapper.writeValueAsString(PostDtoBuilder.startBuilding().build()))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner.login").value(USER_ALICE))
                .andReturn();
        String addStr = addPostRequest.getResponse().getContentAsString();
        LOGGER.info(addStr);
        PostDTO added = objectMapper.readValue(addStr, PostDTO.class);

        // check post present in my posts
        MvcResult getMyPostsRequest = mockMvc.perform(
                get(Constants.Uls.API+Constants.Uls.POST+Constants.Uls.MY)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andReturn();
        String strListPosts = getMyPostsRequest.getResponse().getContentAsString();
        LOGGER.info(strListPosts);
        List<PostDTO> posts = objectMapper.readValue(strListPosts, new TypeReference<List<PostDTO>>(){});
        Assert.assertTrue("I should can see my created post",
                posts.stream().anyMatch(postDTO -> postDTO.getTitle().equals("default new post title")));

        // check foreign post not present in my posts
        Assert.assertFalse("foreign post shouldn't be in my posts",
                posts.stream().anyMatch(postDTO -> postDTO.getTitle().startsWith("generated_post")));

        // check Alice can update her post
        final String updatedTitle = "updated title";
        added.setTitle(updatedTitle);
        MvcResult updatePostRequest = mockMvc.perform(
                put(Constants.Uls.API+Constants.Uls.POST)
                        .content(objectMapper.writeValueAsString(added))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(updatedTitle))
                .andExpect(jsonPath("$.owner.login").value(USER_ALICE))
                .andReturn();
        LOGGER.info(updatePostRequest.getResponse().getContentAsString());
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

    @Test
    public void testAnonymousCannotUpdatePost() throws Exception {
        final long foreignPostId = 1001;
        PostDTO postDTO = PostDtoBuilder.startBuilding().id(foreignPostId).build();

        MvcResult addPostRequest = mockMvc.perform(
                put(Constants.Uls.API+Constants.Uls.POST)
                        .content(objectMapper.writeValueAsString(postDTO))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isUnauthorized())
                .andReturn();
        LOGGER.info(addPostRequest.getResponse().getContentAsString());
    }


    @WithUserDetails(USER_ALICE)
    @Test
    public void testUserCannotUpdateForeignPost() throws Exception {
        final long foreignPostId = 1000;

        MvcResult getPostRequest = mockMvc.perform(
                get(Constants.Uls.API_PUBLIC+Constants.Uls.POST+"/"+foreignPostId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canEdit").value(false))
                .andExpect(jsonPath("$.canDelete").value(false))
                .andReturn();
        String getStr = getPostRequest.getResponse().getContentAsString();
        LOGGER.info(getStr);
        PostDTO foreign = objectMapper.readValue(getStr, PostDTO.class);


        MvcResult addPostRequest = mockMvc.perform(
                put(Constants.Uls.API+Constants.Uls.POST)
                        .content(objectMapper.writeValueAsString(foreign))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isForbidden())
                .andReturn();
        String addStr = addPostRequest.getResponse().getContentAsString();
        LOGGER.info(addStr);

    }

    @WithUserDetails(USER_ALICE)
    @Test
    public void testUserCannotRecreateExistsPost() throws Exception {
        final long foreignPostId = 1001;

        PostDTO postDTO = PostDtoBuilder.startBuilding().id(foreignPostId).build();
        MvcResult addPostRequest = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.POST)
                        .content(objectMapper.writeValueAsString(postDTO))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isBadRequest())
                .andReturn();
        String addStr = addPostRequest.getResponse().getContentAsString();
        LOGGER.info(addStr);
    }

    @WithUserDetails(USER_ADMIN)
    @Test
    public void testAdminCanUpdateForeignPost() throws Exception {
        final long foreignPostId = 1000;

        MvcResult getPostRequest = mockMvc.perform(
                get(Constants.Uls.API_PUBLIC+Constants.Uls.POST+"/"+foreignPostId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canEdit").value(true))
                .andExpect(jsonPath("$.canDelete").value(true))
                .andReturn();
        String getStr = getPostRequest.getResponse().getContentAsString();
        LOGGER.info(getStr);
        PostDTO foreign = objectMapper.readValue(getStr, PostDTO.class);

        final String title = "title updated by admin";
        foreign.setTitle(title);
        MvcResult updatePostRequest = mockMvc.perform(
                put(Constants.Uls.API+Constants.Uls.POST)
                        .content(objectMapper.writeValueAsString(foreign))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(title))
                .andReturn();
        String addStr = updatePostRequest.getResponse().getContentAsString();
        LOGGER.info(addStr);

    }

}
