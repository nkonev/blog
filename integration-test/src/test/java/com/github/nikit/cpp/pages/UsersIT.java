package com.github.nikit.cpp.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.github.nikit.cpp.integration.AbstractItTestRunner;
import com.github.nikit.cpp.pages.object.LoginModal;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.clearBrowserCookies;
import static com.codeborne.selenide.Selenide.open;
import static com.github.nikit.cpp.IntegrationTestConstants.Pages.USERS_LIST;

/**
 * Тест на список пользователей, управляемый админом
 * Created by nik on 12.07.17.
 */
@Ignore
public class UsersIT extends AbstractItTestRunner {

    @Before
    public void before(){
        clearBrowserCookies();

    }


    public static class UsersPage {
        private String urlPrefix;
        public UsersPage(String urlPrefix) {
            this.urlPrefix = urlPrefix;
        }
        public void openPage() {
            open(urlPrefix+USERS_LIST);
        }
        public void openPage(long page) {
            open(urlPrefix+USERS_LIST + "?page="+page);
        }

    }

    @Test
    public void testPagination() throws Exception {
        UsersPage usersPage = new UsersPage(urlPrefix);
        usersPage.openPage();

        LoginModal loginModal = new LoginModal(user, password);
        loginModal.login();
    }

    @Test
    public void testOpenSecondPage() throws Exception {
        UsersPage usersPage = new UsersPage(urlPrefix);
        usersPage.openPage(2);

        LoginModal loginModal = new LoginModal(user, password);
        loginModal.login();

        // тут мы ожидаем что пользователь(админ) увидит список пользователей без перезагрузки
        Selenide.refresh();

        $("li .page-item,.active a").shouldHave(Condition.text("2"));
    }

}
