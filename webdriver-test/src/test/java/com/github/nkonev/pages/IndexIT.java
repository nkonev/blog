package com.github.nkonev.pages;

import com.codeborne.selenide.Condition;
import com.github.nkonev.CommonTestConstants;
import com.github.nkonev.controllers.PostController;
import com.github.nkonev.dto.PostDTO;
import com.github.nkonev.dto.UserAccountDetailsDTO;
import com.github.nkonev.integration.AbstractItTestRunner;
import com.github.nkonev.pages.object.IndexPage;
import com.github.nkonev.security.BlogUserDetailsService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import java.util.Date;

import static com.codeborne.selenide.Selenide.$;

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

    @After
    public void after() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testWebsocketPush() throws Exception {
        IndexPage indexPage = new IndexPage(urlPrefix);
        indexPage.openPage();
        indexPage.contains("Lorem Ipsum является стандартной \"рыбой\" для текстов на латинице с начала XVI века.");

        PostDTO postDTO = new PostDTO(0, "Added via websocket", "Пост, пришедший через вебсокет " + new Date(), "image.png");
        UserAccountDetailsDTO accountDetailsDTO = authenticateAs(CommonTestConstants.USER_ADMIN);
        PostDTO added = postController.addPost(accountDetailsDTO, postDTO);

        indexPage.contains("Added via websocket");

        indexPage.setSearchString("234");
        indexPage.contains("generated_post_<u>234</u>");
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

        indexPage.contains("<u>generated</u>_<u>post</u>_<u>1765</u>");

        indexPage.sendEnd();

        Assert.assertEquals(1, indexPage.posts().size());
    }

}
