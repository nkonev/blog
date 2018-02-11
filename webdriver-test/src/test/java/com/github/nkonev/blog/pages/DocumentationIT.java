package com.github.nkonev.blog.pages;

import com.github.nkonev.blog.integration.AbstractItTestRunner;
import com.github.nkonev.blog.junit.matcher.RegexpMatcher;
import org.junit.Assert;
import org.junit.Test;
import java.util.Map;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.github.nkonev.blog.webdriver.IntegrationTestConstants.Pages.INDEX_HTML;

/**
 * Created by nik on 06.06.17.
 */
public class DocumentationIT extends AbstractItTestRunner {

    private static final String ID_DOC = "#a-doc";


    @Test
    public void testDocumentationIsPresent() throws Exception {
        open(urlPrefix+ INDEX_HTML);

        $(ID_DOC).click();
        $("body").shouldHave(text("Blog API Reference"));
    }

    @Test
    public void testGitVersion() throws Exception {
        String url = urlPrefix + "/git.json";

        Map<String, String> response = (Map<String, String>)testRestTemplate.getForObject(url, Object.class);

        Assert.assertThat(response.get("build.version"), RegexpMatcher.regexp("\\d+.*"));
    }
}
