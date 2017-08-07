package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.AbstractUtTestRunner;
import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.TestConstants;
import com.github.nikit.cpp.dto.CreateUserDTO;
import com.github.nikit.cpp.entity.redis.UserConfirmationToken;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationControllerTest.class);

    @Value("${spring.mail.port}")
    private int port;

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


        // confirm
        // http://www.icegreen.com/greenmail/javadocs/com/icegreen/greenmail/util/Retriever.html
        try (Retriever r = new Retriever(greenMail.getImap())) {
            IMAPMessage imapMessage = (IMAPMessage) r.getMessages(email)[0];
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
    }

    @Test
    public void testRegistrationPasswordIsRequired() throws Exception {

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
        userConfirmationTokenRepository.save(token1); // if random token exists we delete it

        // create /confirm?uuid=<uuid>
        String uri = UriComponentsBuilder.fromUriString(Constants.Uls.CONFIRM).queryParam(Constants.Uls.UUID, tokenUuid).build().toUriString();

        mockMvc.perform(get(uri))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, "/confirm/registration/user-not-found"))
        ;

    }

}
