package com.github.nikit.cpp.pages;

import com.codeborne.selenide.Selenide;
import com.github.nikit.cpp.IntegrationTestConstants;
import com.github.nikit.cpp.integration.AbstractItTestRunner;
import com.github.nikit.cpp.pages.object.LoginModal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.clearBrowserCookies;
import static com.codeborne.selenide.Selenide.open;
import static com.github.nikit.cpp.IntegrationTestConstants.Pages.INDEX_HTML;
import static com.github.nikit.cpp.IntegrationTestConstants.Pages.USER;

/**
 * Тест на страницу профиля
 * Created by nik on 06.06.17.
 */
public class UserProfileIT extends AbstractItTestRunner {

    @Value(IntegrationTestConstants.USER_ID)
    private int userId;

    @Before
    public void before(){
        clearBrowserCookies();
    }

    public static class UserProfilePage {
        private String urlPrefix;
        public UserProfilePage(String urlPrefix) {
            this.urlPrefix = urlPrefix;
        }
        public void openPage(int userId) {
            open(urlPrefix+USER+"/"+userId);
        }
    }

    @Test
    public void useSeeThisIsYouAfterLogin() throws Exception {
        UserProfilePage userPage = new UserProfilePage(urlPrefix);
        userPage.openPage(userId);

        LoginModal loginModal = new LoginModal(user, password);
        loginModal.openLoginModal();
        loginModal.login();

        $(".profile").shouldHave(text("Это вы"));

        Selenide.refresh();

        $(".profile").shouldHave(text("Это вы"));

    }

}
