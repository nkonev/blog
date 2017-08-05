package com.github.nikit.cpp.pages;

import com.codeborne.selenide.Condition;
import com.github.nikit.cpp.integration.AbstractItTestRunner;
import com.github.nikit.cpp.pages.object.LoginModal;
import org.junit.Before;
import org.junit.Test;

import static com.codeborne.selenide.Selenide.*;
import static com.github.nikit.cpp.IntegrationTestConstants.Pages.USERS_LIST;

/**
 * Тест на список пользователей, управляемый админом
 * Created by nik on 12.07.17.
 */

public class UserListIT extends AbstractItTestRunner {

    @Before
    public void before(){
        clearBrowserCookies();
    }


    public static class UsersPage {
        public static final String USERS_CONTAINER_SELECTOR = "#user-list";

        public static final String DISABLED_CLASS = "disabled";
        public static final String PREV_PAGE_LI_SELECTOR = "li.prev-item";
        public static final String NEXT_PAGE_LI_SELECTOR = "li.next-item";
        public static final String PREV_PAGE_A_SELECTOR = "a.prev-link-item";
        public static final String NEXT_PAGE_A_SELECTOR = "a.next-link-item";
        private String urlPrefix;
        public UsersPage(String urlPrefix) {
            this.urlPrefix = urlPrefix;
        }

        /**
         * Открыть страницу в браузере
         */
        public void openPage() {
            open(urlPrefix+USERS_LIST);
        }
        /**
         * Открыть страницу page в браузере
         */
        public void openPage(int page) {
            open(urlPrefix+USERS_LIST + "?page="+page);
        }

        /**
         * Проверяет активную страницу в пагинаторе
         * @param expected
         */
        public void assertActivePaginatorPage(int expected) {
            $("li .page-item,.active a").shouldHave(Condition.text(String.valueOf(expected)));
        }

        public void goNextPaginatorPage() {
            $(NEXT_PAGE_A_SELECTOR).exists();
            $(NEXT_PAGE_A_SELECTOR).click();
        }

        /**
         * Переходин пагинатором на страницу. Данная страница должна быть видна.
         * @param paginatorPage
         */
        public void goNthPaginatorPage(int paginatorPage) {
            $$("a.page-link-item").findBy(Condition.text(String.valueOf(paginatorPage))).click();
        }
    }

    @Test
    public void testPagination() throws Exception {
        UsersPage usersPage = new UsersPage(urlPrefix);
        usersPage.openPage();

        LoginModal loginModal = new LoginModal(user, password);
        loginModal.login();
        $(UsersPage.PREV_PAGE_LI_SELECTOR).shouldHave(Condition.cssClass(UsersPage.DISABLED_CLASS));

        $(UsersPage.USERS_CONTAINER_SELECTOR).shouldHave(Condition.text("admin"));
        $(UsersPage.USERS_CONTAINER_SELECTOR).shouldHave(Condition.text("nikita"));
        $(UsersPage.USERS_CONTAINER_SELECTOR).shouldHave(Condition.text("alice"));
        $(UsersPage.USERS_CONTAINER_SELECTOR).shouldHave(Condition.text("bob"));

        $(UsersPage.USERS_CONTAINER_SELECTOR).shouldHave(Condition.text("generated_user_0"));
        $(UsersPage.USERS_CONTAINER_SELECTOR).shouldHave(Condition.text("generated_user_4"));
        $(UsersPage.USERS_CONTAINER_SELECTOR).shouldHave(Condition.text("generated_user_5"));

        usersPage.goNextPaginatorPage();
        $(UsersPage.USERS_CONTAINER_SELECTOR).shouldHave(Condition.text("generated_user_6"));

        loginModal.logout();
        $(UsersPage.USERS_CONTAINER_SELECTOR).shouldNotHave(Condition.text("generated_user_6"));

        loginModal.openLoginModal();
        loginModal.login();
        usersPage.goNthPaginatorPage(5);
        $(UsersPage.USERS_CONTAINER_SELECTOR).shouldHave(Condition.text("generated_user_40"));

        usersPage.goNthPaginatorPage(101);
        $(UsersPage.USERS_CONTAINER_SELECTOR).shouldHave(Condition.text("generated_user_1000"));
        $(UsersPage.NEXT_PAGE_LI_SELECTOR).shouldHave(Condition.cssClass(UsersPage.DISABLED_CLASS));
    }

    @Test
    public void testOpenSecondPage() throws Exception {
        UsersPage usersPage = new UsersPage(urlPrefix);
        final int page = 2;
        usersPage.openPage(page);

        LoginModal loginModal = new LoginModal(user, password);
        loginModal.login();

        // тут мы ожидаем что пользователь(админ) увидит список пользователей без перезагрузки
//        Selenide.refresh();

        usersPage.assertActivePaginatorPage(page);
    }

    @Test
    public void userCanTwiceLoginLogout() throws Exception {
        UsersPage userPage = new UsersPage(urlPrefix);
        userPage.openPage();

        LoginModal loginModal = new LoginModal(user, password);

        loginModal.login();

        loginModal.logout();

        loginModal.openLoginModal();

        loginModal.login();
    }

}
