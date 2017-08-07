package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.AbstractUtTestRunner;
import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.TestConstants;
import com.github.nikit.cpp.dto.CreateUserDTO;
import com.github.nikit.cpp.entity.jpa.UserAccount;
import com.github.nikit.cpp.entity.redis.UserConfirmationToken;
import com.github.nikit.cpp.repo.jpa.UserAccountRepository;
import com.github.nikit.cpp.repo.redis.UserConfirmationTokenRepository;
import com.github.nikit.cpp.security.SecurityConfig;
import com.github.nikit.cpp.util.UrlParser;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

import javax.mail.Message;
import java.net.URI;
import java.util.UUID;
import static com.github.nikit.cpp.security.SecurityConfig.PASSWORD_PARAMETER;
import static com.github.nikit.cpp.security.SecurityConfig.USERNAME_PARAMETER;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class RegistrationControllerTest extends AbstractUtTestRunner {

    @Autowired
    private UserConfirmationTokenRepository userConfirmationTokenRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationControllerTest.class);

    @Rule
    public GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    @Test
    public void testConfirmationSuccess() throws Exception {
        final String email = "newbie@example.com";
        final String username = "newbie";
        final String password = "password";

        CreateUserDTO createUserDTO = new CreateUserDTO(username, null, password, email);

        // register
        MvcResult createAccountRequest = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.REGISTER)
                        .content(objectMapper.writeValueAsString(createUserDTO))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andReturn();
        String createAccountStr = createAccountRequest.getResponse().getContentAsString();
        LOGGER.info(createAccountStr);

        // login unconfirmed fail
        mockMvc.perform(
                post(SecurityConfig.API_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param(USERNAME_PARAMETER, username)
                        .param(PASSWORD_PARAMETER, password)
                        .with(csrf())
        )
                .andExpect(status().isUnauthorized());

        // user lost email and reissues token
        {
            long tokenCountBeforeResend = userConfirmationTokenRepository.count();
            mockMvc.perform(
                    post(Constants.Uls.API + Constants.Uls.RESEND_CONFIRMATION_EMAIL + "?email=" + email)
                            .with(csrf())
            )
                    .andExpect(status().isOk());
            Assert.assertEquals(tokenCountBeforeResend+1, userConfirmationTokenRepository.count());
        }

        // confirm
        // http://www.icegreen.com/greenmail/javadocs/com/icegreen/greenmail/util/Retriever.html
        try (Retriever r = new Retriever(greenMail.getImap())) {
            Message[] messages = r.getMessages(email);
            Assert.assertEquals("backend should sent two email: a) during registration; b) during confirmation token reissue",2, messages.length);
            IMAPMessage imapMessage = (IMAPMessage)messages[1];
            String content = (String) imapMessage.getContent();

            String parsedUrl = UrlParser.parseUrlFromMessage(content);

            String tokenUuidString = UriComponentsBuilder.fromUri(new URI(parsedUrl)).build().getQueryParams().get(Constants.Uls.UUID).get(0);
            Assert.assertTrue(userConfirmationTokenRepository.exists(tokenUuidString));

            // perform confirm
            mockMvc.perform(get(parsedUrl)).andExpect(status().isOk());
            Assert.assertFalse(userConfirmationTokenRepository.exists(tokenUuidString));
        }

        // login confirmed ok
        mockMvc.perform(
                post(SecurityConfig.API_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param(USERNAME_PARAMETER, username)
                        .param(PASSWORD_PARAMETER, password)
                        .with(csrf())
        )
                .andExpect(status().isOk());

        // resend for already confirmed does nothing
        {
            long tokenCountBeforeResend = userConfirmationTokenRepository.count();
            mockMvc.perform(
                    post(Constants.Uls.API + Constants.Uls.RESEND_CONFIRMATION_EMAIL + "?email=" + email)
                            .with(csrf())
            )
                    .andExpect(status().isOk());
            Assert.assertEquals(tokenCountBeforeResend, userConfirmationTokenRepository.count());
        }
    }

    @Test
    public void testRegistrationPasswordIsRequired() throws Exception {
        final String email = "newbie@example.com";
        final String username = "newbie";

        CreateUserDTO createUserDTO = new CreateUserDTO(username, null, null, email);

        // register
        MvcResult createAccountResult = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.REGISTER)
                        .content(objectMapper.writeValueAsString(createUserDTO))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation error"))
                .andExpect(jsonPath("$.message").value("validation error, see validationErrors[]"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("password"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("may not be empty"))
                .andReturn();
        String stringResponse = createAccountResult.getResponse().getContentAsString();
        LOGGER.info(stringResponse);

    }

    @Test
    public void testRegistrationPasswordNotEnoughLong() throws Exception {
        final String email = "newbie@example.com";
        final String username = "newbie";

        CreateUserDTO createUserDTO = new CreateUserDTO(username, null, "123", email);

        // register
        MvcResult createAccountResult = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.REGISTER)
                        .content(objectMapper.writeValueAsString(createUserDTO))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation error"))
                .andExpect(jsonPath("$.message").value("validation error, see validationErrors[]"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("password"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("size must be between 6 and 30"))
                .andReturn();
        String stringResponse = createAccountResult.getResponse().getContentAsString();
        LOGGER.info(stringResponse);

    }

    @Test
    public void testRegistrationUserWithSameLoginAlreadyPresent() throws Exception {
        final String email = "newbie@example.com";
        final String username = TestConstants.USER_ALICE;
        final String password = "password";

        CreateUserDTO createUserDTO = new CreateUserDTO(username, null, password, email);

        // register
        MvcResult createAccountResult = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.REGISTER)
                        .content(objectMapper.writeValueAsString(createUserDTO))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("user already present"))
                .andExpect(jsonPath("$.message").value("User with login 'alice' is already present"))
                .andReturn();
        String stringResponse = createAccountResult.getResponse().getContentAsString();
        LOGGER.info(stringResponse);
    }

    @Test
    public void testRegistrationUserWithSameEmailAlreadyPresent() throws Exception {
        final String email = "alice@example.com";
        final String username = "newbie";
        final String password = "password";

        UserAccount userAccountBefore = userAccountRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("user account not found in test"));

        CreateUserDTO createUserDTO = new CreateUserDTO(username, null, password, email);

        // register
        MvcResult createAccountResult = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.REGISTER)
                        .content(objectMapper.writeValueAsString(createUserDTO))
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andReturn();
        String stringResponse = createAccountResult.getResponse().getContentAsString();
        LOGGER.info(stringResponse);

        UserAccount userAccountAfter = userAccountRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("user account not found in test"));

        // check that initial user account is not affected
        Assert.assertEquals(userAccountBefore.getId(), userAccountAfter.getId());
        Assert.assertEquals(userAccountBefore.getAvatar(), userAccountAfter.getAvatar());
        Assert.assertEquals(TestConstants.USER_ALICE, userAccountBefore.getUsername());
        Assert.assertEquals(userAccountBefore.getUsername(), userAccountAfter.getUsername());
        Assert.assertEquals(userAccountBefore.getPassword(), userAccountAfter.getPassword());
        Assert.assertEquals(userAccountBefore.getRoles(), userAccountAfter.getRoles());
    }


    @Test
    public void testConfirmationTokenNotFound() throws Exception {
        String token = UUID.randomUUID().toString(); // create random token
        userConfirmationTokenRepository.delete(token); // if random token exists we delete it

        // create /confirm?uuid=<uuid>
        String uri = UriComponentsBuilder.fromUriString(Constants.Uls.CONFIRM).queryParam(Constants.Uls.UUID, token).build().toUriString();

        mockMvc.perform(get(uri))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, "/confirm/registration/token-not-found"))
        ;
    }

    @Test
    public void testConfirmationUserNotFound() throws Exception {
        String tokenUuid = UUID.randomUUID().toString(); // create random token
        UserConfirmationToken token1 = new UserConfirmationToken(tokenUuid, -999L, 180);
        userConfirmationTokenRepository.save(token1); // save it

        // create /confirm?uuid=<uuid>
        String uri = UriComponentsBuilder.fromUriString(Constants.Uls.CONFIRM).queryParam(Constants.Uls.UUID, tokenUuid).build().toUriString();

        mockMvc.perform(get(uri))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, "/confirm/registration/user-not-found"))
        ;

    }

    @Test
    public void testAttackerCannotStealLockedUserAccount() throws Exception {
        String bobEmail = "bob@example.com";
        UserAccount bob = userAccountRepository.findByEmail(bobEmail).orElseThrow(()->new RuntimeException("bob not found in test"));

        bob.setLocked(true);
        bob = userAccountRepository.save(bob);

        // attacker
        long tokenCountBeforeResend = userConfirmationTokenRepository.count();
        mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.RESEND_CONFIRMATION_EMAIL+"?email="+bobEmail)
                    .with(csrf())
        )
                .andExpect(status().isOk());
        Assert.assertEquals("new token shouldn't appear when attacker attempts reactivate banned(locked) user", tokenCountBeforeResend, userConfirmationTokenRepository.count());
    }

}
