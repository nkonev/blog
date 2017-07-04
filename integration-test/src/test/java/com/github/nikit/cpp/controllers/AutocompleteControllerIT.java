package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.integration.AbstractItTestRunner;
import org.junit.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.github.nikit.cpp.IntegrationTestConstants.AUTOCOMPLETE_HTML;

/**
 * Created by nik on 06.06.17.
 */
public class AutocompleteControllerIT extends AbstractItTestRunner {

    @Test
    public void testUni() throws Exception {

        open(urlPrefix+AUTOCOMPLETE_HTML);
        $(".v-autocomplete-input").setValue("Uni");
        $(".v-autocomplete-list").shouldHave(text("United Arab Emirates"));

        //TimeUnit.SECONDS.sleep(9999);
    }
}
