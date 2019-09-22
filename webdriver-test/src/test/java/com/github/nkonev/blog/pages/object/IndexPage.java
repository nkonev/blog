package com.github.nkonev.blog.pages.object;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.WebDriverRunner;
import com.github.nkonev.blog.integration.AbstractItTestRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.github.nkonev.blog.webdriver.IntegrationTestConstants.Pages.INDEX_HTML;

public class IndexPage {
    private String urlPrefix;
    private WebDriver driver;
    private static final String BODY = "body";
    public static final String POST = ".post";
    public static final String POST_LIST = ".post-list";
    public static final String ADD_POST = ".fab div";
    public IndexPage(String urlPrefix) {
        this.urlPrefix = urlPrefix;
        this.driver = WebDriverRunner.getWebDriver();
    }

    /**
     * Открыть страницу в браузере
     */
    public void openPage() {
        open(urlPrefix+INDEX_HTML);
    }

    public void openPage(String query) {
        open(urlPrefix+INDEX_HTML+"?q="+query);
    }

    public void contains(String s) {
        $(POST_LIST).shouldHave(Condition.text(s));
    }

    public void setSearchString(String s) {
        $("input.search-input").setValue(s);
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
        $(ADD_POST).shouldBe(AbstractItTestRunner.CLICKABLE).click();
    }

    public void clickSettings(){
        $(BODY).find(By.linkText("Settings")).shouldBe(AbstractItTestRunner.CLICKABLE).click();
    }

    public void clearSearchButton() {
        $(".search-clear").shouldBe(AbstractItTestRunner.CLICKABLE).click();
    }
}
