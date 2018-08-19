package com.github.nkonev.blog.pages.object;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.github.nkonev.blog.FailoverUtils;
import com.github.nkonev.blog.integration.AbstractItTestRunner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import java.util.concurrent.TimeUnit;
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

    public LoginModal() {
    }


    public void login() {
        SelenideElement modal = getValidModal();
        WebElement modal2 = modal.getWrappedElement();

        modal.find("input#username").setValue(user);
        modal.find("input#password").setValue(password);
        modal.find(ID_SUBMIT).shouldBe(AbstractItTestRunner.CLICKABLE).click();
        $(".user-profile-nav .login").shouldHave(text("" + user));

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
        $(byText("login")).shouldBe(AbstractItTestRunner.CLICKABLE).click();
    }

    public void loginFacebook() {
        SelenideElement modal = getValidModal();
        modal.find("#btn-submit-facebook").click();
    }

    private SelenideElement getValidModal() {
        SelenideElement modal = $(".v--modal-box");
        modal.waitUntil(Condition.visible, 5*1000);
        modal.shouldHave(text("Please login"));
        return modal;
    }
}
