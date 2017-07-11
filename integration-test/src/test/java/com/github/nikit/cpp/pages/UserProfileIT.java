package com.github.nikit.cpp.pages;

import com.github.nikit.cpp.integration.AbstractItTestRunner;
import com.github.nikit.cpp.pages.object.LoginModal;
import org.junit.Before;
import org.junit.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.clearBrowserCookies;
import static com.codeborne.selenide.Selenide.open;
import static com.github.nikit.cpp.IntegrationTestConstants.Pages.INDEX_HTML;

/**
 * Тест на страницу профиля
 * Created by nik on 06.06.17.
 */
public class UserProfileIT extends AbstractItTestRunner {

    private static final String PROFILE_LINK = "a[href='/profile']";


    @Before
    public void before(){
        clearBrowserCookies();
    }

    public static class UserProfilePage {
        private String urlPrefix;
        public UserProfilePage(String urlPrefix) {
            this.urlPrefix = urlPrefix;
        }
        public void openPage() {
            open(urlPrefix+INDEX_HTML);
        }
        public void openProtectedProfilePage() {
            $(PROFILE_LINK).click();
        }
    }

    @Test
    public void useSeeHisLoginAfterSuccessfulLogin() throws Exception {
        UserProfilePage userPage = new UserProfilePage(urlPrefix);
        userPage.openPage();

        LoginModal loginModal = new LoginModal(user, password);

        userPage.openProtectedProfilePage();

        loginModal.login();
    }

    @Test
    public void userCanTwiceLoginLogout() throws Exception {
        UserProfilePage userPage = new UserProfilePage(urlPrefix);
        userPage.openPage();

        LoginModal loginModal = new LoginModal(user, password);

        userPage.openProtectedProfilePage();

        loginModal.login();

        loginModal.logout();

        loginModal.openLoginModal();

        loginModal.login();
    }

}
