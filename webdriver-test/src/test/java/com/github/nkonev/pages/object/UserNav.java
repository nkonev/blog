package com.github.nkonev.pages.object;

import com.codeborne.selenide.Condition;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.github.nkonev.integration.AbstractItTestRunner.CLICKABLE;

/**
 * Created by nik on 15.07.17.
 */
public class UserNav {
    public static void open() {
        $(".user-profile-nav .multiselect").shouldBe(CLICKABLE).click();
    }

    public static void exit() {
        final int waitFor = 10;
        $(byText("exit"))
                .waitUntil(Condition.exist, 1000 * waitFor)
                .waitUntil(Condition.enabled, 1000 * waitFor)
                .waitUntil(Condition.visible, 1000 * waitFor)
                .click();
    }

    public static void profile() {
        $(byText("profile")).shouldBe(CLICKABLE).click();
    }

}
