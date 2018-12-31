package com.github.nkonev.blog.pages.object;

import com.codeborne.selenide.Condition;
import org.openqa.selenium.WebDriver;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.github.nkonev.blog.integration.AbstractItTestRunner.CLICKABLE;
import static com.github.nkonev.blog.webdriver.IntegrationTestConstants.Pages.SETTINGS;

public class SettingsPage {
    public static final String IMG_BG = "#app img.bg";
    private String urlPrefix;
    private WebDriver driver;
    private static final String BODY = "body";

    public SettingsPage(String urlPrefix) {
        this.urlPrefix = urlPrefix;
        this.driver = $(BODY).getWrappedDriver();
    }

    public void openPage() {
        open(urlPrefix+SETTINGS);
    }

    public void setBackgroundImage(String absoluteFilePath) {
        Croppa.setImage(absoluteFilePath);
    }

    public void clickSave() {
        $("button.ok-btn")
                .waitUntil(Condition.exist, 20 * 1000)
                .waitUntil(Condition.visible, 20 * 1000)
                .waitUntil(Condition.enabled, 20 * 1000)
                .shouldBe(CLICKABLE).click(); // this can open login modal if you unauthenticated
    }

    public void waitForBackgroundImageWillSet() {
        $(SettingsPage.IMG_BG).waitUntil(Condition.have(Condition.attribute("src")), 10000);
    }
}
