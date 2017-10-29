package com.github.nkonev.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nkonev.AbstractUtTestRunner;
import com.github.nkonev.Constants;
import com.github.nkonev.repo.jpa.PostRepository;
import com.github.nkonev.utils.PageUtils;
import com.github.nkonev.TestConstants;
import com.github.nkonev.dto.PostDTO;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import static org.hamcrest.core.Is.is;
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

    public static class PostDtoBuilder {
        public static class Instance {
            private final PostDTO postDTO;
            {
                postDTO = new PostDTO(
                        0,
                        "default new post title",
                        "default new post text",
                        "https://postgrespro.ru/img/logo_mono.png",
                        LocalDateTime.now(ZoneOffset.UTC)
                );
            }
            public PostDTO build() {
                return postDTO;
            }

            public Instance id(long id) {
                postDTO.setId(id);
                return this;
            }

            public Instance text(String s) {
                postDTO.setText(s);
                return this;
            }
        }

        public static Instance startBuilding() {
            return new Instance();
        }
    }

    public static final long FOREIGN_POST = 1000;


    @Test
    public void testAnonymousCanGetPostsAndItsLimiting() throws Exception {
        MvcResult getPostsRequest = mockMvc.perform(
                get(Constants.Uls.API+Constants.Uls.POST)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(PageUtils.DEFAULT_SIZE))
                .andReturn();
    }

    @Test
    public void testTrimmed() throws Exception {
        MvcResult getPostsRequest = mockMvc.perform(
                get(Constants.Uls.API+Constants.Uls.POST+"?searchString= ")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(PageUtils.DEFAULT_SIZE))
                .andReturn();
    }

    @Test
    public void testFulltext() throws Exception {
        MvcResult getPostsRequest = mockMvc.perform(
                get(Constants.Uls.API+Constants.Uls.POST+"?searchString=частый рыбами posted")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(PageUtils.DEFAULT_SIZE))
                .andExpect(jsonPath("$.[0].title").value("generated_<u>post</u>_2000"))
                .andExpect(jsonPath("$.[0].text").value("Lorem Ipsum - это текст-\"<b>рыба</b>\", <b>часто</b> используемый в печати и вэб-дизайне. Lorem Ipsum является"))
                .andReturn();
    }

    @Test
    public void test404() throws Exception {
        MvcResult getPostsRequest = mockMvc.perform(
                get(Constants.Uls.API+Constants.Uls.POST+"/1005001")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("data not found"))
                .andExpect(jsonPath("$.message").value("Post 1005001 not found"))
                .andReturn();
    }


    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void testUserCanAddAndUpdateAndCannotDeletePost() throws Exception {
        MvcResult addPostRequest = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.POST)
                        .content(objectMapper.writeValueAsString(PostDtoBuilder.startBuilding().build()))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner.login").value(TestConstants.USER_ALICE))
                .andExpect(jsonPath("$.canEdit").value(true))
                .andExpect(jsonPath("$.canDelete").value(false))
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
                .andExpect(jsonPath("$.owner.login").value(TestConstants.USER_ALICE))
                .andExpect(jsonPath("$.canEdit").value(true))
                .andExpect(jsonPath("$.canDelete").value(false))
                .andReturn();
        LOGGER.info(updatePostRequest.getResponse().getContentAsString());


        MvcResult deleteResult = mockMvc.perform(
                delete(Constants.Uls.API+Constants.Uls.POST+"/"+added.getId()).with(csrf())
        )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    public void testAnonymousCannotAddPostUnit() throws Exception {
        postController.addPost(null, PostDtoBuilder.startBuilding().build());
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
        final long foreignPostId = FOREIGN_POST;
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

    @Test
    public void testAnonymousCannotDeletePost() throws Exception {
        final long foreignPostId = FOREIGN_POST;
        PostDTO postDTO = PostDtoBuilder.startBuilding().id(foreignPostId).build();

        MvcResult addPostRequest = mockMvc.perform(
                delete(Constants.Uls.API+Constants.Uls.POST+"/"+foreignPostId).with(csrf())
                        .content(objectMapper.writeValueAsString(postDTO))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isUnauthorized())
                .andReturn();
        LOGGER.info(addPostRequest.getResponse().getContentAsString());
    }



    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void testUserCannotUpdateForeignPost() throws Exception {
        final long foreignPostId = FOREIGN_POST;

        MvcResult getPostRequest = mockMvc.perform(
                get(Constants.Uls.API+Constants.Uls.POST+"/"+foreignPostId)
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

    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void testUserCannotDeleteForeignPost() throws Exception {
        final long foreignPostId = FOREIGN_POST;
        PostDTO postDTO = PostDtoBuilder.startBuilding().id(foreignPostId).build();

        MvcResult addPostRequest = mockMvc.perform(
                delete(Constants.Uls.API+Constants.Uls.POST+"/"+foreignPostId).with(csrf())
                        .content(objectMapper.writeValueAsString(postDTO))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isForbidden())
                .andReturn();
        LOGGER.info(addPostRequest.getResponse().getContentAsString());
    }


    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void testUserCannotRecreateExistsPost() throws Exception {
        final long foreignPostId = FOREIGN_POST;

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

    @WithUserDetails(TestConstants.USER_ADMIN)
    @Test
    public void testAdminCanUpdateForeignPost() throws Exception {
        final long foreignPostId = FOREIGN_POST;

        MvcResult getPostRequest = mockMvc.perform(
                get(Constants.Uls.API+Constants.Uls.POST+"/"+foreignPostId)
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

    @WithUserDetails(TestConstants.USER_ADMIN)
    @Test
    public void testAdminCanDeleteForeignPost() throws Exception {
        final long foreignPostId = FOREIGN_POST;

        // add some comments
        {
            MvcResult addCommentRequest = mockMvc.perform(
                    post(Constants.Uls.API+Constants.Uls.POST+"/"+foreignPostId+"/"+Constants.Uls.COMMENT)
                            .content(objectMapper.writeValueAsString(CommentControllerTest.CommentDtoBuilder.startBuilding().build()))
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .with(csrf())
            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.owner.login").value(TestConstants.USER_ADMIN))
                    .andExpect(jsonPath("$.canEdit").value(true))
                    .andExpect(jsonPath("$.canDelete").value(true))
                    .andReturn();
        }


        MvcResult deletePostRequest = mockMvc.perform(
                delete(Constants.Uls.API+Constants.Uls.POST+"/"+foreignPostId).with(csrf())
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andReturn();

        LOGGER.info(deletePostRequest.getResponse().getContentAsString());
    }


    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void xssText() throws Exception {
        MvcResult addPostRequest = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.POST)
                        .content(objectMapper.writeValueAsString(PostDtoBuilder.startBuilding().text("Harmless <script>alert('XSS')</script>text").build()))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner.login").value(TestConstants.USER_ALICE))
                .andExpect(jsonPath("$.text").value("Harmless text"))
                .andReturn();
        String addStr = addPostRequest.getResponse().getContentAsString();
        LOGGER.info(addStr);
        PostDTO added = objectMapper.readValue(addStr, PostDTO.class);

    }
}
