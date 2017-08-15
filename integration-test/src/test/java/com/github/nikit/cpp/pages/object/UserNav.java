package com.github.nikit.cpp.pages.object;

import com.codeborne.selenide.Condition;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.github.nikit.cpp.integration.AbstractItTestRunner.CLICKABLE;

/**
 * Created by nik on 15.07.17.
 */
public class UserNav {
    public static void open() {
        $(".user-profile-nav .multiselect").shouldBe(CLICKABLE).click();
    }

    public static void exit() {
        $(byText("exit")).shouldBe(CLICKABLE).click();
    }

    public static void profile() {
        $(byText("profile")).shouldBe(CLICKABLE).click();
    }

}
