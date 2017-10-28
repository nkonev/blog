package com.github.nkonev.pages.object;

import com.codeborne.selenide.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        $(".user-profile-nav .dropobj")
//                .waitUntil(Condition.enabled, USER_NAV_TIMEOUT)
//                .waitUntil(Condition.visible, USER_NAV_TIMEOUT)
                .hover();
    }

    public static void exit() {
        $(byText("Logout")).click();
    }

    public static void profile() {
        $(byText("Profile")).shouldBe(CLICKABLE).click();
    }

    public static String getAvatarUrl(){
        return $(".user-profile-nav .avatar").getAttribute("src");
    }
}
