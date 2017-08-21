package com.github.nikit.cpp.pages;

import com.github.nikit.cpp.integration.AbstractItTestRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.springframework.beans.factory.annotation.Autowired;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.github.nikit.cpp.IntegrationTestConstants.Pages.AUTOCOMPLETE;

/**
 * Created by nik on 06.06.17.
 */
public class AutocompleteControllerIT extends AbstractItTestRunner {

    @Test
    public void testUni() throws Exception {

        open(urlPrefix+ AUTOCOMPLETE);

        final String SELECTOR = "input.sbx-google__input";

        $(SELECTOR).shouldBe(CLICKABLE).getWrappedElement().sendKeys("Uni");

        WebElement e = $(SELECTOR).shouldBe(CLICKABLE).getWrappedElement();

        // root cause -- send custom KEYUP event for vue-instant (https://stackoverflow.com/questions/25012780/how-do-i-enter-text-in-textfield-with-onkeyup-onfocus-javascript/25030094#25030094)
        // e.sendKeys("t"); // not necessary
        new Actions(driver).keyDown(e, Keys.CONTROL).keyUp(e, Keys.CONTROL).perform();

        $(".vue-instant__suggestions").shouldBe(CLICKABLE).shouldHave(text("United Arab Emirates"));

        //TimeUnit.SECONDS.sleep(9999);
    }

}
