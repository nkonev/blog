package com.github.nkonev.pages.object;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.github.nkonev.IntegrationTestConstants.Pages.INDEX_HTML;
import static com.github.nkonev.integration.AbstractItTestRunner.CLICKABLE;

public class IndexPage {
    private String urlPrefix;
    private WebDriver driver;
    private static final String BODY = "body";
    public static final String POST = ".post";
    public static final String POST_LIST = ".post-list";
    public static final String ADD_POST = ".fab a";
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

    public void clickAddPost() {
        $(ADD_POST).shouldBe(CLICKABLE).click();
    }

    public void clearSearchButton() {
        $("#clear-search").shouldBe(CLICKABLE).click();
    }
}
