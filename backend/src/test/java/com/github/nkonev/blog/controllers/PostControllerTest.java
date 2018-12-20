package com.github.nkonev.blog.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nkonev.blog.AbstractUtTestRunner;
import com.github.nkonev.blog.Constants;
import com.github.nkonev.blog.TestConstants;
import com.github.nkonev.blog.config.CustomConfig;
import com.github.nkonev.blog.dto.PostDTO;
import com.github.nkonev.blog.dto.PostDTOWithAuthorization;
import com.github.nkonev.blog.dto.UserAccountDTO;
import com.github.nkonev.blog.dto.UserAccountDetailsDTO;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import com.github.nkonev.blog.services.SeoCacheService;
import com.github.nkonev.blog.utils.PageUtils;
import org.hamcrest.core.StringStartsWith;
import org.junit.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import static com.github.nkonev.blog.utils.SeoCacheKeyUtils.RENDERTRON_HTML;
import static com.github.nkonev.blog.utils.SeoCacheKeyUtils.getRedisKeyForIndex;
import static com.github.nkonev.blog.utils.SeoCacheKeyUtils.getRedisKeyHtmlForPost;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

public class PostControllerTest extends AbstractUtTestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostControllerTest.class);

    @Autowired
    private PostController postController;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SeoCacheService seoCacheService;

    private MockRestServiceServer mockServer;

    @Autowired
    private CustomConfig customConfig;

    @BeforeEach
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterEach
    public void tearDown(){
        redisTemplate.delete(RENDERTRON_HTML+"*");
    }

    public static class PostDtoBuilder {
        public static class Instance {
            private final PostDTO postDTO;
            {
                postDTO = new PostDTO(
                        0,
                        "default new post title",
                        "default new post text",
                        "https://postgrespro.ru/img/logo_mono.png",
                        null,
                        null,
                        new UserAccountDTO()
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

    public static final long FOREIGN_POST = 50;


    @Test
    public void testAnonymousCanGetPostsAndItsLimiting() throws Exception {
        MvcResult getPostsRequest = mockMvc.perform(
                MockMvcRequestBuilders.get(Constants.Urls.API+ Constants.Urls.POST)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(PageUtils.DEFAULT_SIZE))
                .andExpect(jsonPath("$[0].commentCount").value(1))
                .andExpect(jsonPath("$[1].commentCount").value(501))
                .andExpect(jsonPath("$[2].commentCount").value(0))
                .andReturn();
        String getStr = getPostsRequest.getResponse().getContentAsString();
        LOGGER.info(getStr);
    }

    @Test
    public void testAnonymousCanGetRandomPosts() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get(Constants.Urls.API+ Constants.Urls.POST + Constants.Urls.RANDOM)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testTrimmed() throws Exception {
        MvcResult getPostsRequest = mockMvc.perform(
                get(Constants.Urls.API+ Constants.Urls.POST+"?searchString= ")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(PageUtils.DEFAULT_SIZE))
                .andReturn();
    }

    @Test
    public void testFulltextSearch() throws Exception {
        MvcResult getPostsRequest = mockMvc.perform(
                get(Constants.Urls.API+ Constants.Urls.POST+"?searchString=рыбами")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(PageUtils.DEFAULT_SIZE))
                .andExpect(jsonPath("$.[0].title").value("generated_post_100"))
                .andExpect(jsonPath("$.[0].text").value("Lorem Ipsum - это текст-\"<b>рыба</b>\", часто используемый в печати и вэб-дизайне.... Lorem Ipsum является стандартной \"<b>рыбой</b>\" для текстов на латинице с начала XVI века."))
                .andReturn();
    }

    @DisplayName("Префиксный поиск")
    @Test
    public void testPrefixSearch() throws Exception {
        MvcResult getPostsRequest = mockMvc.perform(
                get(Constants.Urls.API+ Constants.Urls.POST+"?searchString=исп")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(PageUtils.DEFAULT_SIZE))
                .andExpect(jsonPath("$.[0].title").value("generated_post_100"))
                .andExpect(jsonPath("$.[0].text").value("Lorem Ipsum - это текст-\"рыба\", часто <b>используемый</b> в печати и вэб-дизайне.... В то время некий безымянный печатник создал большую коллекцию размеров и форм шрифтов, <b>используя</b> Lorem Ipsum для распечатки образцов.... листов Letraset с образцами Lorem Ipsum в 60-х годах и, в более недавнее время, программы электронной вёрстки типа Aldus PageMaker, в шаблонах которых <b>используется</b>"))
                .andReturn();
    }

    @Test
    public void testSlopSearch() throws Exception {
        MvcResult getPostsRequest = mockMvc.perform(
                get(Constants.Urls.API+ Constants.Urls.POST+"?searchString=является начать")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(PageUtils.DEFAULT_SIZE))
                .andExpect(jsonPath("$.[0].title").value("generated_post_100"))
                .andExpect(jsonPath("$.[0].text").value("Lorem Ipsum <b>является</b> стандартной \"рыбой\" для текстов на латинице с <b>начала</b> XVI века."))
                .andReturn();
    }

    @Disabled
    @Test
    public void testContainsSearch() throws Exception {
        MvcResult getPostsRequest = mockMvc.perform(
                get(Constants.Urls.API+ Constants.Urls.POST+"?searchString=psum")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(PageUtils.DEFAULT_SIZE))
                .andExpect(jsonPath("$.[0].title").value("generated_post_100"))
                .andExpect(jsonPath("$.[0].text").value("Lorem <b>Ipsum</b> - это текст-\"рыба\", часто используемый в печати и вэб-дизайне.... Lorem <b>Ipsum</b> является стандартной \"рыбой\" для текстов на латинице с начала XVI века.... В то время некий безымянный печатник создал большую коллекцию размеров и форм шрифтов, используя Lorem <b>Ipsum</b> для распечатки образцов.... Lorem <b>Ipsum</b> не только успешно пережил без заметных изменений пять веков, но и перешагнул в электронный дизайн.... вёрстки типа Aldus PageMaker, в шаблонах которых используется Lorem <b>Ipsum</b>."))
                .andReturn();
    }


    @WithUserDetails(TestConstants.USER_ADMIN)
    @Test
    public void testFulltextSearchHostPort() throws Exception {
        final String newPostRendered = "<body>Post Rendered</body>";
        mockServer.expect(requestTo(new StringStartsWith("http://rendertron.example.com:3000/"+customConfig.getBaseUrl()+"/post/")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(newPostRendered, MediaType.TEXT_HTML));

        final String newIndexRendered = "<body>Index Rendered</body>";
        mockServer.expect(requestTo("http://rendertron.example.com:3000/"+customConfig.getBaseUrl()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(newIndexRendered, MediaType.TEXT_HTML));

        UserAccountDetailsDTO userAccountDetailsDTO = (UserAccountDetailsDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        postController.updatePost(userAccountDetailsDTO, new PostDTO(50L, "edited for search host port", "A new host for test www.google.com:80 with port too", "", null, null, null));
        postRepository.flush();

        MvcResult getPostsRequest = mockMvc.perform(
                get(Constants.Urls.API+ Constants.Urls.POST+"?searchString=www.google.com:80")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].title").value("edited for search host port"))
                .andExpect(jsonPath("$.[0].text").value("A new host for test <b>www.google.com</b>:<b>80</b> with port too"))
                .andReturn();
    }


    @Test
    public void test404() throws Exception {
        MvcResult getPostsRequest = mockMvc.perform(
                get(Constants.Urls.API+ Constants.Urls.POST+"/1005001")
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("data not found"))
                .andExpect(jsonPath("$.message").value("Post 1005001 not found"))
                .andReturn();
    }


    @Test
    public void test404xml() throws Exception {
        MvcResult getPostsRequest = mockMvc.perform(
                get(Constants.Urls.API+ Constants.Urls.POST+"/1005001")
                        .accept(MediaType.APPLICATION_XML_VALUE)
        )
                .andExpect(status().isNotFound())
                .andExpect(xpath("/BlogError/error").string("data not found"))
                .andExpect(xpath("/BlogError/message").string("Post 1005001 not found"))
                .andReturn();
    }


    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void testUserCanAddAndUpdateAndCannotDeletePost() throws Exception {
        final String newPostRendered = "<body>Post Rendered</body>";
        mockServer.expect(ExpectedCount.times(2), requestTo(new StringStartsWith("http://rendertron.example.com:3000/"+customConfig.getBaseUrl()+"/post/")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(newPostRendered, MediaType.TEXT_HTML));

        final String newIndexRendered = "<body>Index Rendered</body>";
        mockServer.expect(ExpectedCount.times(2), requestTo("http://rendertron.example.com:3000/"+customConfig.getBaseUrl()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(newIndexRendered, MediaType.TEXT_HTML));


        final String oldDataIndex = "<html>bad old index data</html>";
        redisTemplate.opsForValue().set(getRedisKeyForIndex(), oldDataIndex);

        MvcResult addPostRequest = mockMvc.perform(
                post(Constants.Urls.API+ Constants.Urls.POST)
                        .content(objectMapper.writeValueAsString(PostDtoBuilder.startBuilding().build()))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner.login").value(TestConstants.USER_ALICE))
                .andExpect(jsonPath("$.canEdit").value(true))
                .andExpect(jsonPath("$.canDelete").value(false))
                .andExpect(jsonPath("$.createDateTime").isNotEmpty())
                .andExpect(jsonPath("$.editDateTime").isEmpty())
                .andReturn();

        Assert.assertFalse(oldDataIndex.equals(redisTemplate.opsForValue().get(getRedisKeyForIndex())));
        Assert.assertEquals(newIndexRendered, redisTemplate.opsForValue().get(getRedisKeyForIndex()));

        long id = objectMapper.readValue(addPostRequest.getResponse().getContentAsString(), PostDTOWithAuthorization.class).getId();
        Assert.assertTrue(redisTemplate.hasKey(getRedisKeyHtmlForPost(id)));

        String addStr = addPostRequest.getResponse().getContentAsString();
        LOGGER.info(addStr);
        PostDTO added = objectMapper.readValue(addStr, PostDTO.class);

        // check post present in my posts
        MvcResult getMyPostsRequest = mockMvc.perform(
                get(Constants.Urls.API+ Constants.Urls.POST+ Constants.Urls.MY)
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
        final String oldCachedPost = "<html>old post data</html>";
        added.setTitle(updatedTitle);
        redisTemplate.opsForValue().set(getRedisKeyHtmlForPost(added.getId()), oldCachedPost);
        MvcResult updatePostRequest = mockMvc.perform(
                put(Constants.Urls.API+ Constants.Urls.POST)
                        .content(objectMapper.writeValueAsString(added))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(updatedTitle))
                .andExpect(jsonPath("$.owner.login").value(TestConstants.USER_ALICE))
                .andExpect(jsonPath("$.canEdit").value(true))
                .andExpect(jsonPath("$.canDelete").value(false))
                .andExpect(jsonPath("$.createDateTime").isNotEmpty())
                .andExpect(jsonPath("$.editDateTime").isNotEmpty())
                .andReturn();
        LOGGER.info(updatePostRequest.getResponse().getContentAsString());
        Assert.assertFalse(oldCachedPost.equals(redisTemplate.opsForValue().get(getRedisKeyHtmlForPost(added.getId()))));
        Assert.assertEquals(newPostRendered, redisTemplate.opsForValue().get(getRedisKeyHtmlForPost(id)));

        MvcResult deleteResult = mockMvc.perform(
                delete(Constants.Urls.API+ Constants.Urls.POST+"/"+added.getId()).with(csrf())
        )
                .andExpect(status().isForbidden())
                .andReturn();

        mockServer.verify();
    }

    /*@WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void testEditDateSetsAfterEdit() throws Exception {
        final String newPostRendered = "<body>Post Rendered</body>";
        mockServer.expect(ExpectedCount.times(2), requestTo(new StringStartsWith("http://rendertron.example.com:3000/"+customConfig.getBaseUrl()+"/post/")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(newPostRendered, MediaType.TEXT_HTML));

        final String newIndexRendered = "<body>Index Rendered</body>";
        mockServer.expect(ExpectedCount.times(2), requestTo("http://rendertron.example.com:3000/"+customConfig.getBaseUrl()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(newIndexRendered, MediaType.TEXT_HTML));

        MvcResult addPostRequest = mockMvc.perform(
                post(Constants.Urls.API+ Constants.Urls.POST)
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

        // check Alice can update her post
        final String updatedTitle = "updated title";
        added.setTitle(updatedTitle);
        MvcResult updatePostRequest = mockMvc.perform(
                put(Constants.Urls.API+ Constants.Urls.POST)
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
    }*/


    @Test
    public void testAnonymousCannotAddPostUnit() throws Exception {
        Assertions.assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            postController.addPost(null, PostDtoBuilder.startBuilding().build());
        });
    }

    @Test
    public void testAnonymousCannotAddPost() throws Exception {
        MvcResult addPostRequest = mockMvc.perform(
                post(Constants.Urls.API+ Constants.Urls.POST)
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
                put(Constants.Urls.API+ Constants.Urls.POST)
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
                delete(Constants.Urls.API+ Constants.Urls.POST+"/"+foreignPostId).with(csrf())
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
                get(Constants.Urls.API+ Constants.Urls.POST+"/"+foreignPostId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canEdit").value(false))
                .andExpect(jsonPath("$.canDelete").value(false))
                .andReturn();
        String getStr = getPostRequest.getResponse().getContentAsString();
        LOGGER.info(getStr);
        PostDTO foreign = objectMapper.readValue(getStr, PostDTO.class);


        MvcResult addPostRequest = mockMvc.perform(
                put(Constants.Urls.API+ Constants.Urls.POST)
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
                delete(Constants.Urls.API+ Constants.Urls.POST+"/"+foreignPostId).with(csrf())
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
                post(Constants.Urls.API+ Constants.Urls.POST)
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
        final String newPostRendered = "<body>Post Rendered</body>";
        mockServer.expect(requestTo(new StringStartsWith("http://rendertron.example.com:3000/"+customConfig.getBaseUrl()+"/post/")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(newPostRendered, MediaType.TEXT_HTML));

        final String newIndexRendered = "<body>Index Rendered</body>";
        mockServer.expect(requestTo("http://rendertron.example.com:3000/"+customConfig.getBaseUrl()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(newIndexRendered, MediaType.TEXT_HTML));


        final long foreignPostId = FOREIGN_POST;

        MvcResult getPostRequest = mockMvc.perform(
                get(Constants.Urls.API+ Constants.Urls.POST+"/"+foreignPostId)
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
                put(Constants.Urls.API+ Constants.Urls.POST)
                        .content(objectMapper.writeValueAsString(foreign))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(title))
                .andReturn();
        String addStr = updatePostRequest.getResponse().getContentAsString();
        LOGGER.info(addStr);
        mockServer.verify();
    }

    @WithUserDetails(TestConstants.USER_ADMIN)
    @Test
    public void testAdminCanDeleteForeignPost() throws Exception {
        final long foreignPostId = FOREIGN_POST;

        // add some comments
        {
            MvcResult addCommentRequest = mockMvc.perform(
                    post(Constants.Urls.API+ Constants.Urls.POST+"/"+foreignPostId+"/"+ Constants.Urls.COMMENT)
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

        final String newIndexRendered = "<body>Index Rendered</body>";
        mockServer.expect(requestTo("http://rendertron.example.com:3000/"+customConfig.getBaseUrl()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(newIndexRendered, MediaType.TEXT_HTML));

        MvcResult deletePostRequest = mockMvc.perform(
                delete(Constants.Urls.API+ Constants.Urls.POST+"/"+foreignPostId).with(csrf())
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andReturn();

        LOGGER.info(deletePostRequest.getResponse().getContentAsString());
    }


    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void xssText() throws Exception {
        final String newPostRendered = "<body>Post Rendered</body>";
        mockServer.expect(requestTo(new StringStartsWith("http://rendertron.example.com:3000/"+customConfig.getBaseUrl()+"/post/")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(newPostRendered, MediaType.TEXT_HTML));

        final String newIndexRendered = "<body>Index Rendered</body>";
        mockServer.expect(requestTo("http://rendertron.example.com:3000/"+customConfig.getBaseUrl()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(newIndexRendered, MediaType.TEXT_HTML));


        MvcResult addPostRequest = mockMvc.perform(
                post(Constants.Urls.API+ Constants.Urls.POST)
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
