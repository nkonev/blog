package com.github.nkonev.pages.object;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.github.nkonev.integration.AbstractItTestRunner.CLICKABLE;

/**
 * Created by nik on 15.07.17.
 */
public class UserNav {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserNav.class);
    private static final int USER_NAV_TIMEOUT = 20000;
    public static void open() {
        $(".user-profile-nav .multiselect")
                .waitUntil(Condition.enabled, USER_NAV_TIMEOUT)
                .waitUntil(Condition.visible, USER_NAV_TIMEOUT)
                .click();
    }

    public static void exit() {
        for (int i = 1; i <= 2; ++i) {
            try {
                LOGGER.info("Trying to press exit");
                $(byText("exit")).click();
            } catch (Throwable e) {
                LOGGER.warn("Next attempt to press exit");
                continue;
            }
            break;
        }
    }

    public static void profile() {
        $(byText("profile")).shouldBe(CLICKABLE).click();
    }

    public static String getAvatarUrl(){
        return $(".user-profile-nav .avatar").getAttribute("src");
    }
}
