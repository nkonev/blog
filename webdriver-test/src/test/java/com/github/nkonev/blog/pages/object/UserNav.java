package com.github.nkonev.blog.pages.object;

import com.codeborne.selenide.Condition;
import com.github.nkonev.blog.integration.AbstractItTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

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
        $(byText("Profile")).shouldBe(AbstractItTestRunner.CLICKABLE).click();
    }

    public static String getAvatarUrl(){
        String val =  $(".user-profile-nav .vue-avatar--wrapper").getWrappedElement().getCssValue("background");

        Pattern pattern = Pattern.compile("(\\()\"(.*)(\"\\))");
        Matcher m = pattern.matcher(val);
        m.find();
        return m.group(2);
    }

    public static String getLogin() {
        return $(".user-profile-nav .login").text();
    }

}
