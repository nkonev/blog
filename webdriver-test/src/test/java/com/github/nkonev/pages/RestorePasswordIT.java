package com.github.nkonev.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.github.nkonev.Constants;
import com.github.nkonev.integration.AbstractItTestRunner;
import com.github.nkonev.pages.object.LoginModal;
import com.github.nkonev.util.UrlParser;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.Retriever;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.sun.mail.imap.IMAPMessage;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;

import javax.mail.Message;
import java.net.URI;

import static com.codeborne.selenide.Selenide.$;
import static com.github.nkonev.CommonTestConstants.NON_DELETABLE_POST_TITLE;

public class RestorePasswordIT extends AbstractItTestRunner {
    // http://127.0.0.1:8080/restore-password

    @Rule
    public GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    @Test
    public void restorePassword() throws Exception {
        final String user = "forgive-password-user";
        final String email = "forgive-password-user@example.com";
        final String newPassword = "olololo1234";
        Selenide.open(urlPrefix + "/restore-password");

        $("input#email").shouldBe(CLICKABLE).setValue(email);
        $("button#send").shouldBe(CLICKABLE).click();

        $(".check-your-email").waitUntil(Condition.text("check your email"), 1000 * 10);

        try (Retriever r = new Retriever(greenMail.getImap())) {
            Message[] messages = r.getMessages(email);
            Assert.assertEquals("backend should sent one email",1, messages.length);
            IMAPMessage imapMessage = (IMAPMessage)messages[0];
            String content = (String) imapMessage.getContent();

            String parsedUrl = UrlParser.parseUrlFromMessage(content);

            // perform confirm
            driver.navigate().to(parsedUrl);
            $(".password-reset-enter-new").should(Condition.text("Please enter new password"));
        }

        $(".password-reset-enter-new input#new-password").waitUntil(Condition.visible, 1000 * 10);
        $(".password-reset-enter-new input#new-password").setValue(newPassword);
        $(".password-reset-enter-new button#set-password").shouldBe(CLICKABLE).click();

        $("body").waitUntil(Condition.text("Now you can login with new password"), 1000 * 10);

        LoginModal loginModal = new LoginModal(user, newPassword);
        loginModal.openLoginModal();
        loginModal.login();
        $("body").shouldHave(Condition.text(NON_DELETABLE_POST_TITLE));
    }

}
