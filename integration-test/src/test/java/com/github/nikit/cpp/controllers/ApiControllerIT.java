package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.integration.AbstractItTestRunner;
import org.junit.Before;
import org.junit.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.clearBrowserCookies;
import static com.codeborne.selenide.Selenide.open;

/**
 * Created by nik on 06.06.17.
 */
public class ApiControllerIT extends AbstractItTestRunner {

    private static final String ID_API = "#a-api";
    private static final String ID_SUBMIT = "#btn-submit";

    private static final String HTML = "/static/index.html";

    @Before
    public void before(){
        clearBrowserCookies();
    }

    @Test
    public void testHelloOnlyAuthenticated() throws Exception {
        open(urlPrefix+HTML);

        $(ID_API).click();
        $("body").shouldHave(text("Пожалуйста, представьтесь"));

        $("input#username").setValue(user);
        $("input#password").setValue(password);

        $(ID_SUBMIT).click();

        open(urlPrefix+HTML);
        $(ID_API).click();

        $("body").shouldHave(text("Happy Halloween, "+user));
    }
}
