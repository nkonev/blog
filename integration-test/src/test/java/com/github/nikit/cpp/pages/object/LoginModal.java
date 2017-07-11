package com.github.nikit.cpp.pages.object;

import com.codeborne.selenide.Selenide;
import com.github.nikit.cpp.pages.UserProfileIT;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

/**
 * Created by nik on 12.07.17.
 */
public class LoginModal {
    private static final String ID_SUBMIT = "#btn-submit";

    private String user, password;

    public LoginModal(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public void login() {
        $("body").shouldHave(text("Пожалуйста, представьтесь"));
        $("input#username").setValue(user);
        $("input#password").setValue(password);
        $(ID_SUBMIT).click();
        $(".user-profile-nav-login").shouldHave(text("" + user));
    }

    public void logout() {
        $(".multiselect").click();
        $(byText("exit")).click();
    }

    public void openLoginModal() {
        $(byText("login")).click();
    }
}
