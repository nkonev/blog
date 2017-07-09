package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.IntegrationTestConstants;
import com.github.nikit.cpp.integration.AbstractItTestRunner;
import org.junit.Before;
import org.junit.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.clearBrowserCookies;
import static com.codeborne.selenide.Selenide.open;

/**
 * Created by nik on 06.06.17.
 */
public class ApiControllerIT extends AbstractItTestRunner {

    private static final String ID_API = "#a-api";
    private static final String ID_SUBMIT = "#btn-submit";


    @Before
    public void before(){
        clearBrowserCookies();
    }

    public static class LoginModal {
        private String user, password;
        public LoginModal(String user, String password){
            this.user = user;
            this.password = password;
        }

        public void callProtectedMethod() {
            $(ID_API).click();
        }

        public void login() {
            $("body").shouldHave(text("Пожалуйста, представьтесь"));
            $("input#username").setValue(user);
            $("input#password").setValue(password);
            $(ID_SUBMIT).click();
            $(".user-profile-nav-login").shouldHave(text(""+user));
        }

        public void logout() {
            $(".multiselect").click();
            $(byText("exit")).click();
        }

        public void openLoginModal() {
            $(byText("login")).click();
        }
    }

    @Test
    public void useSeeHisLoginAfterSuccessfulLogin() throws Exception {
        open(urlPrefix+ IntegrationTestConstants.INDEX_HTML);

        LoginModal loginModal = new LoginModal(user, password);

        loginModal.callProtectedMethod();

        loginModal.login();
    }

    @Test
    public void userCanTwiceLoginLogout() throws Exception {
        open(urlPrefix+ IntegrationTestConstants.INDEX_HTML);

        LoginModal loginModal = new LoginModal(user, password);

        loginModal.callProtectedMethod();

        loginModal.login();

        loginModal.logout();

        loginModal.openLoginModal();

        loginModal.login();
    }

}
