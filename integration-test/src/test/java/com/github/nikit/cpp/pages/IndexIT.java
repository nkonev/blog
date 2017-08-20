package com.github.nikit.cpp.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.github.nikit.cpp.controllers.WebSocketController;
import com.github.nikit.cpp.integration.AbstractItTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.springframework.beans.factory.annotation.Autowired;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.github.nikit.cpp.IntegrationTestConstants.Pages.INDEX_HTML;

public class IndexIT extends AbstractItTestRunner {

    @Autowired
    private WebSocketController webSocketController;

    public static class IndexPage {
        private String urlPrefix;
        private WebDriver driver;
        private static final String BODY = "body";
        public static final String POST = ".post";
        public static final String POST_LIST = ".post-list";
        public IndexPage(String urlPrefix) {
            this.urlPrefix = urlPrefix;
            this.driver = $(BODY).getWrappedDriver();
        }

        /**
         * Открыть страницу в браузере
         */
        public void openPage() {
            open(urlPrefix+INDEX_HTML);
        }

        public void contains(String s) {
            $(POST_LIST).shouldHave(Condition.text(s));
        }

        public void setSearchString(String s) {
            $("input#search").setValue(s);
        }

        public void sendEnd() {
            $(BODY).sendKeys(Keys.END);
            // firing key down / up
            WebElement e = $(BODY).getWrappedElement();
            new Actions(driver).keyDown(e, Keys.CONTROL).keyUp(e, Keys.CONTROL).perform();
        }

        public ElementsCollection posts() {
            return $(POST_LIST).findAll(POST);
        }
    }

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
