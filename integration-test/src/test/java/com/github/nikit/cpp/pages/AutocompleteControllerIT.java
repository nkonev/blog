package com.github.nikit.cpp.pages;

import com.github.nikit.cpp.integration.AbstractItTestRunner;
import org.junit.Ignore;
import org.junit.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.github.nikit.cpp.IntegrationTestConstants.Pages.AUTOCOMPLETE;

/**
 * Created by nik on 06.06.17.
 */
public class AutocompleteControllerIT extends AbstractItTestRunner {

    @Ignore
    @Test
    public void testUni() throws Exception {

        open(urlPrefix+ AUTOCOMPLETE);
        $(".countries-autocomplete-input").setValue("Uni");
        $(".v-autocomplete-list").shouldHave(text("United Arab Emirates"));

        //TimeUnit.SECONDS.sleep(9999);
    }
}
