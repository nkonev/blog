package com.github.nkonev.blog.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.github.nkonev.blog.CommonTestConstants;
import com.github.nkonev.blog.controllers.PostController;
import com.github.nkonev.blog.dto.OwnerDTO;
import com.github.nkonev.blog.dto.PostDTO;
import com.github.nkonev.blog.dto.UserAccountDTO;
import com.github.nkonev.blog.dto.UserAccountDetailsDTO;
import com.github.nkonev.blog.integration.AbstractItTestRunner;
import com.github.nkonev.blog.pages.object.IndexPage;
import com.github.nkonev.blog.security.BlogUserDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import static com.codeborne.selenide.Selenide.$;
import static com.github.nkonev.blog.utils.TimeUtil.getNowUTC;

public class IndexIT extends AbstractItTestRunner {

    @Autowired
    private PostController postController;

    @Autowired
    private BlogUserDetailsService blogUserDetailsService;

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

    @AfterEach
    public void after() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testWebsocketPush() throws Exception {
        IndexPage indexPage = new IndexPage(urlPrefix);
        indexPage.openPage();
        indexPage.contains("Lorem Ipsum является стандартной \"рыбой\" для текстов на латинице с начала XVI века.");

        PostDTO postDTO = new PostDTO(0, "Added via websocket", "Пост, пришедший через вебсокет " + new Date(), "image.png", getNowUTC(), null, new OwnerDTO(), false, null);
        UserAccountDetailsDTO accountDetailsDTO = authenticateAs(CommonTestConstants.USER_ADMIN);
        PostDTO added = postController.addPost(accountDetailsDTO, postDTO);

        indexPage.contains("Added via websocket");

        indexPage.setSearchString("generated_post_34");
        indexPage.contains("generated_post_34");
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

        final String searchQuery = "generated_post_65";

        indexPage.setSearchString(searchQuery); // request that respond one result

        indexPage.contains(searchQuery);

        indexPage.sendEnd();

        Assertions.assertEquals(1, indexPage.posts().size());
    }

    @Test
    public void testUrlChangingDuringSearch() throws Exception {

        IndexPage indexPage = new IndexPage(urlPrefix);
        indexPage.openPage();

        final String searchQuery = "generated_post_66";
        indexPage.setSearchString(searchQuery); // request that respond one result

        indexPage.contains(searchQuery);

        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.endsWith(searchQuery));


        indexPage.openPage(searchQuery);
        indexPage.contains(searchQuery);
    }

}
