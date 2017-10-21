package com.github.nkonev.pages.object;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.github.nkonev.FailoverUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import java.util.concurrent.TimeUnit;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.github.nkonev.integration.AbstractItTestRunner.CLICKABLE;

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
        SelenideElement modal = $(".v--modal-box");
        WebElement modal2 = modal.getWrappedElement();

        modal.shouldHave(text("Пожалуйста, представьтесь"));
        modal.find("input#username").setValue(user);
        modal.find("input#password").setValue(password);
        modal.find(ID_SUBMIT).shouldBe(CLICKABLE).click();
        $(".user-profile-nav-login").shouldHave(text("" + user));

        WebDriver driver = WebDriverRunner.getWebDriver();
        final Wait<WebDriver> wait = new FluentWait<>(driver)
                .withMessage("still present")
                .withTimeout(10, TimeUnit.SECONDS)
                .pollingEvery(1, TimeUnit.SECONDS);
        wait.until(webDriver -> ExpectedConditions.invisibilityOf(modal2));
    }

    public void logout() {
        FailoverUtils.retry(2, () -> {
            UserNav.open();
            UserNav.exit();
            return null;
        });
    }

    public void openLoginModal() {
        $(byText("login")).shouldBe(CLICKABLE).click();
    }
}
