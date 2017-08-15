package com.github.nikit.cpp.pages.object;

import com.codeborne.selenide.Condition;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

/**
 * Created by nik on 15.07.17.
 */
public class UserNav {
    public static void open() {
        $(".user-profile-nav .multiselect").click();
    }

    public static void exit() {
        // http://www.seleniumhq.org/docs/04_webdriver_advanced.jsp#expected-conditions
        // clickable https://seleniumhq.github.io/selenium/docs/api/java/org/openqa/selenium/support/ui/ExpectedConditions.html#elementToBeClickable-org.openqa.selenium.By-
        $(byText("exit")).should(be(enabled), be(visible));
        $(byText("exit")).click();
    }

    public static void profile() {
        $(byText("profile")).should(be(enabled), be(visible));
        $(byText("profile")).click();
    }

}
