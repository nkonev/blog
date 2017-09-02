package com.github.nikit.cpp.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.dto.EditUserDTO;
import com.github.nikit.cpp.integration.AbstractItTestRunner;
import com.github.nikit.cpp.pages.object.IndexPage;
import com.github.nikit.cpp.pages.object.LoginModal;
import com.github.nikit.cpp.repo.redis.UserConfirmationTokenRepository;
import com.github.nikit.cpp.security.SecurityConfig;
import com.github.nikit.cpp.util.UrlParser;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.Retriever;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.sun.mail.imap.IMAPMessage;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

import javax.mail.Message;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.github.nikit.cpp.security.SecurityConfig.PASSWORD_PARAMETER;
import static com.github.nikit.cpp.security.SecurityConfig.USERNAME_PARAMETER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RegistrationIT extends AbstractItTestRunner {

    @Autowired
    private UserConfirmationTokenRepository userConfirmationTokenRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationIT.class);

    @Rule
    public GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP)/*.withConfiguration(new GreenMailConfiguration().withDisabledAuthentication())*/;

    @Test
    public void testConfirmationSuccess() throws Exception {
        // TimeUnit.SECONDS.sleep(10);
        final String email = "newbiewd@example.com";
        final String username = "newbiewd";
        final String password = "password";

        // register
        driver.navigate().to(urlPrefix+"/registration");

        $(byText("Registration")).shouldBe(CLICKABLE).click();
        $(".registration input#login").shouldBe(CLICKABLE).setValue(username);
        $(".registration input#email").shouldBe(CLICKABLE).setValue(email);
        $(".registration input#password").shouldBe(CLICKABLE).setValue(password);
        // Configuration.timeout = 7 * 1000;
        $("button#submit").shouldBe(CLICKABLE).click();

        // we must wait here before invoke greenMail.getImap()
        $(".registration").should(Condition.text("Your confirmation email successfully sent"));

        // Resend
        $(".registration a.registration-confirmation-resend")
                .waitUntil(Condition.visible, 10 * 1000)
                .waitUntil(Condition.enabled, 10 * 1000)
                .click();
        $(".resend-registration-confirmation-token input").shouldBe(Condition.visible).shouldHave(Condition.value(email));
        $(".resend-registration-confirmation-token button").shouldBe(CLICKABLE).click();
        $(".resend-registration-confirmation-token span.email-successfully-sent").shouldBe(Condition.visible);

        // confirm
        try (Retriever r = new Retriever(greenMail.getImap())) {
            Message[] messages = r.getMessages(email);
            Assert.assertEquals("backend should sent one email during registration and I request resend",2, messages.length);
            IMAPMessage imapMessage = (IMAPMessage)messages[1];
            String content = (String) imapMessage.getContent();

            String parsedUrl = UrlParser.parseUrlFromMessage(content);

            String tokenUuidString = UriComponentsBuilder.fromUri(new URI(parsedUrl)).build().getQueryParams().get(Constants.Uls.UUID).get(0);
            Assert.assertTrue(userConfirmationTokenRepository.exists(tokenUuidString));

            // perform confirm
            driver.navigate().to(parsedUrl);
            Assert.assertFalse(userConfirmationTokenRepository.exists(tokenUuidString));
            $(".successfully-confirmed").should(Condition.text("You successfully confirmed, now you can"));
        }

        // login confirmed ok
        $(".successfully-confirmed a").shouldBe(CLICKABLE).click(); // it open login modal
        LoginModal loginModal = new LoginModal(username, password);
        loginModal.login();
    }


}
