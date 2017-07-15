package com.github.nikit.cpp.pages.object;

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
        $(byText("exit")).click();
    }

    public static void profile() {
        $(byText("profile")).click();
    }

}
