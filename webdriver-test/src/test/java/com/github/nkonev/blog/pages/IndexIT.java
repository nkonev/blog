package com.github.nkonev.blog.pages;

import com.codeborne.selenide.Condition;
import com.github.nkonev.blog.CommonTestConstants;
import com.github.nkonev.blog.controllers.PostController;
import com.github.nkonev.blog.dto.PostDTO;
import com.github.nkonev.blog.dto.UserAccountDTO;
import com.github.nkonev.blog.dto.UserAccountDetailsDTO;
import com.github.nkonev.blog.integration.AbstractItTestRunner;
import com.github.nkonev.blog.pages.object.IndexPage;
import com.github.nkonev.blog.pages.object.LoginModal;
import com.github.nkonev.blog.pages.object.UserNav;
import com.github.nkonev.blog.security.BlogUserDetailsService;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selenide.$;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class IndexIT extends AbstractItTestRunner {

    @Autowired
    private PostController postController;

    @Autowired
    private BlogUserDetailsService blogUserDetailsService;

    private static ClientAndServer mockServer;

    private static final int MOCK_SERVER_PORT = 10080;

    @Before
    public void setUp() {
        mockServer = startClientAndServer(MOCK_SERVER_PORT);
    }

    @After
    public void tearDown() throws Exception {
        mockServer.reset();
        mockServer.stop();
    }


    /**
     * You should call SecurityContextHolder.clearContext(); in @After
     * @param username
     * @return
     */
    private UserAccountDetailsDTO authenticateAs(String username) {
        UserAccountDetailsDTO accountDetailsDTO = blogUserDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(accountDetailsDTO, accountDetailsDTO.getPassword(), accountDetailsDTO.getAuthorities());
        SecurityContextImpl securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(usernamePasswordAuthenticationToken);
        SecurityContextHolder.setContext(securityContext);
        return accountDetailsDTO;
    }

    @After
    public void after() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testWebsocketPush() throws Exception {
        IndexPage indexPage = new IndexPage(urlPrefix);
        indexPage.openPage();
        indexPage.contains("Lorem Ipsum является стандартной \"рыбой\" для текстов на латинице с начала XVI века.");

        PostDTO postDTO = new PostDTO(0, "Added via websocket", "Пост, пришедший через вебсокет " + new Date(), "image.png", LocalDateTime.now(ZoneOffset.UTC), new UserAccountDTO());
        UserAccountDetailsDTO accountDetailsDTO = authenticateAs(CommonTestConstants.USER_ADMIN);
        PostDTO added = postController.addPost(accountDetailsDTO, postDTO);

        indexPage.contains("Added via websocket");

        indexPage.setSearchString("234");
        indexPage.contains("generated_post_234");
        indexPage.clearSearchButton();

        added.setTitle("Updated via websocket");
        postController.updatePost(accountDetailsDTO, added);

        indexPage.contains("Updated via websocket");

        postController.deletePost(accountDetailsDTO, added.getId());
        $("body").waitUntil(Condition.not(Condition.text("Updated via websocket")), 1000 * 6);
    }

    /**
     * Test PostList:65
     * if (res.data.length < POSTS_PAGE_SIZE) ...
     */
    @Test
    public void testInfinityBugOnServerRespondLessThanPageSize() throws Exception {

        IndexPage indexPage = new IndexPage(urlPrefix);
        indexPage.openPage();

        indexPage.setSearchString("generated_post_1765"); // request that respond one result

        indexPage.contains("generated_post_1765");

        indexPage.sendEnd();

        Assert.assertEquals(1, indexPage.posts().size());
    }

    @Test
    public void testFacebookLogin() throws InterruptedException {
        mockServer
                .when(request().withPath("/mock/facebook/dialog/oauth")).callback(httpRequest -> {
                    String state = httpRequest.getQueryStringParameters().stream().filter(parameter -> "state".equals(parameter.getName().getValue())).findFirst().get().getValues().get(0).getValue();
                    return response().withHeaders(
                                        new Header(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=\"utf-8\""),
                                        new Header(HttpHeaders.Names.LOCATION, urlPrefix+"/api/login/facebook?code=fake_code&state="+state)
                                ).withStatusCode(302);
                });

        mockServer
                .when(request().withPath("/mock/facebook/oauth/access_token"))
                .respond(response().withHeaders(
                        new Header(HttpHeaders.Names.CONTENT_TYPE, "application/json")
                        ).withStatusCode(200).withBody("{\n" +
                        "  \"access_token\": \"fake-access-token\", \n" +
                        "  \"token_type\": \"bearer\",\n" +
                        "  \"expires_in\":  3600\n" +
                        "}")
                );

        mockServer
                .when(request().withPath("/mock/facebook/me"))
                .respond(response().withHeaders(
                        new Header(HttpHeaders.Names.CONTENT_TYPE, "application/json")
                        ).withStatusCode(200).withBody("{\n" +
                                "  \"id\": \"1234\", \n" +
                                "  \"name\": \"Nikita K\",\n" +
                                "  \"picture\": {\n" +
                                "      \"data\": {\t\n" +
                                "           \"url\": \"https://i.pinimg.com/236x/37/47/62/374762701f2571ffaacba61325d6dbf1--linux-penguin.jpg\"\n" +
                                "        }\n" +
                                "    }"+
                                "}")
                );


        IndexPage indexPage = new IndexPage(urlPrefix);
        indexPage.openPage();

        LoginModal loginModal = new LoginModal(user, password);
        loginModal.openLoginModal();
        loginModal.loginFacebook();

//        Assert.assertEquals("https://i.pinimg.com/236x/37/47/62/374762701f2571ffaacba61325d6dbf1--linux-penguin.jpg", UserNav.getAvatarUrl());
        Assert.assertEquals("Nikita K", UserNav.getLogin());

    }
}
