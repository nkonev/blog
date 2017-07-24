package com.github.nikit.cpp.pages;

import com.codeborne.selenide.Condition;
import com.github.nikit.cpp.controllers.WebSocketController;
import com.github.nikit.cpp.integration.AbstractItTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.clearBrowserCookies;
import static com.codeborne.selenide.Selenide.open;
import static com.github.nikit.cpp.IntegrationTestConstants.Pages.INDEX_HTML;

public class IndexIT extends AbstractItTestRunner {
    @Before
    public void before(){
        clearBrowserCookies();
    }

    @Autowired
    private WebSocketController webSocketController;

    @Test
    public void testWebsocketPush() throws Exception {
        open(urlPrefix+INDEX_HTML);
        $(".post-list").shouldHave(Condition.text("Lorem Ipsum является стандартной \"рыбой\" для текстов на латинице с начала XVI века."));

        webSocketController.greet();
        $(".post-list").shouldHave(Condition.text("Пост, пришедший через вебсокет"));
    }
}
