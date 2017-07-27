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

    public static class IndexPage {
        private String urlPrefix;
        public IndexPage(String urlPrefix) {
            this.urlPrefix = urlPrefix;
        }

        /**
         * Открыть страницу в браузере
         */
        public void openPage() {
            open(urlPrefix+INDEX_HTML);
        }

        public void contains(String s) {
            $(".post-list").shouldHave(Condition.text(s));
        }

        public void setSearchString(String s) {
            $("input#search").setValue(s);
        }
    }

    @Test
    public void testWebsocketPush() throws Exception {

        IndexPage indexPage = new IndexPage(urlPrefix);
        indexPage.openPage();

        indexPage.contains("Lorem Ipsum является стандартной \"рыбой\" для текстов на латинице с начала XVI века.");

        webSocketController.greet();

        indexPage.contains("Пост, пришедший через вебсокет");

        // TODO add js lodash??.debounce
        indexPage.setSearchString("1234");

        indexPage.contains("generated_post_91234");
    }
}
