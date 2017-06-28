package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.IntegrationTestConstants;
import com.github.nikit.cpp.integration.AbstractItTestRunner;
import org.junit.Before;
import org.junit.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

/**
 * Created by nik on 06.06.17.
 */
public class DocumentationIT extends AbstractItTestRunner {

    private static final String ID_DOC = "#a-doc";


    @Before
    public void before(){
        clearBrowserCookies();
    }

    @Test
    public void testDocumentationIsPresent() throws Exception {
        open(urlPrefix+ IntegrationTestConstants.INDEX_HTML);

        $(ID_DOC).click();
        $("body").shouldHave(text("Good parts"));
    }
}
