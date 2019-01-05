package com.github.nkonev.blog.pages.object;

import com.codeborne.selenide.Condition;

import static com.codeborne.selenide.Selenide.$;

public class Dialog {
    public static void clickYes() {
        $(".vue-dialog .vue-dialog-buttons button:nth-of-type(2)").click();
    }

    public static void waitForDialog() {
        $(".vue-dialog").waitUntil(Condition.visible, 7 * 1000, 1000);
    }
}
