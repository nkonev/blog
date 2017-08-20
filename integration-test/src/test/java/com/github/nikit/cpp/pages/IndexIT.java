package com.github.nikit.cpp.pages;

import com.github.nikit.cpp.controllers.WebSocketController;
import com.github.nikit.cpp.integration.AbstractItTestRunner;
import com.github.nikit.cpp.pages.object.IndexPage;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class IndexIT extends AbstractItTestRunner {

    @Autowired
    private WebSocketController webSocketController;

    @Test
    public void testWebsocketPush() throws Exception {

        IndexPage indexPage = new IndexPage(urlPrefix);
        indexPage.openPage();

        indexPage.contains("Lorem Ipsum является стандартной \"рыбой\" для текстов на латинице с начала XVI века.");

        webSocketController.greet();

        indexPage.contains("Пост, пришедший через вебсокет");

        indexPage.setSearchString("234");

        indexPage.contains("generated_post_234");
        indexPage.contains("generated_post_1234");
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

}
