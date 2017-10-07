package com.github.nkonev.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.nkonev.AbstractUtTestRunner;
import com.github.nkonev.Constants;
import com.github.nkonev.TestConstants;
import com.github.nkonev.converter.UserAccountConverter;
import com.github.nkonev.dto.EditUserDTO;
import com.github.nkonev.entity.jpa.UserAccount;
import com.github.nkonev.repo.jpa.UserAccountRepository;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserProfileControllerTest extends AbstractUtTestRunner {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileControllerTest.class);

    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void testGetAliceProfileWhichNotContainsPassword() throws Exception {
        MvcResult getPostsRequest = mockMvc.perform(
                get(Constants.Uls.API+Constants.Uls.PROFILE)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(TestConstants.USER_ALICE))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();
    }

    private UserAccount getUserFromBd(String userName) {
        return userAccountRepository.findByUsername(userName).orElseThrow(() ->  new RuntimeException("User '" + userName + "' not found during test"));
    }

    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void fullyAuthenticatedUserCanChangeHerProfile() throws Exception {
        UserAccount userAccount = getUserFromBd(TestConstants.USER_ALICE);
        final String initialPassword = userAccount.getPassword();

        final String newLogin = "new_alice";

        EditUserDTO edit = UserAccountConverter.convertToEditUserDto(userAccount);
        edit.setLogin(newLogin);

        MvcResult mvcResult = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.PROFILE)
                        .content(objectMapper.writeValueAsString(edit))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(newLogin))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(header().string(Constants.Headers.NEED_REFRESH_PROFILE, "true"))
                .andReturn();

        LOGGER.info(mvcResult.getResponse().getContentAsString());

        Assert.assertEquals("password shouldn't be affected if there isn't set explicitly", initialPassword, getUserFromBd(newLogin).getPassword());
    }

    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void fullyAuthenticatedUserCanChangeHerProfileAndPassword() throws Exception {
        UserAccount userAccount = getUserFromBd(TestConstants.USER_ALICE);
        final String initialPassword = userAccount.getPassword();
        final String newLogin = "new_alice";
        final String newPassword = "new_alice_password";

        EditUserDTO edit = UserAccountConverter.convertToEditUserDto(userAccount);
        edit.setLogin(newLogin);
        edit.setPassword(newPassword);

        MvcResult mvcResult = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.PROFILE)
                        .content(objectMapper.writeValueAsString(edit))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(newLogin))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(header().string(Constants.Headers.NEED_REFRESH_PROFILE, "true"))
                .andReturn();

        LOGGER.info(mvcResult.getResponse().getContentAsString());

        UserAccount afterChange = getUserFromBd(newLogin);
        Assert.assertNotEquals("password should be changed if there is set explicitly", initialPassword, afterChange.getPassword());
        Assert.assertTrue("password should be changed if there is set explicitly", passwordEncoder.matches(newPassword, afterChange.getPassword()));
    }

    /**
     * Bob wants steal Alice's account by rewrite login and set her id
     * @throws Exception
     */
    @Test
    @WithUserDetails(TestConstants.USER_BOB)
    public void fullyAuthenticatedUserCannotChangeForeignProfile() throws Exception {
        UserAccount foreignUserAccount = getUserFromBd(TestConstants.USER_ALICE);
        String foreignUserAccountLogin = foreignUserAccount.getUsername();
        EditUserDTO edit = UserAccountConverter.convertToEditUserDto(foreignUserAccount);

        final String badLogin = "stolen";
        edit.setLogin(badLogin);
        Map<String, Object> userMap = objectMapper.readValue(objectMapper.writeValueAsString(edit), new TypeReference<Map<String, Object>>(){} );
        userMap.put("id", foreignUserAccount.getId());

        MvcResult mvcResult = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.PROFILE)
                        .content(objectMapper.writeValueAsString(userMap))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
        )
                .andReturn();

        LOGGER.info(mvcResult.getResponse().getContentAsString());

        UserAccount foreignPotentiallyAffectedUserAccount = getUserFromBd(TestConstants.USER_ALICE);
        Assert.assertEquals(foreignUserAccountLogin, foreignPotentiallyAffectedUserAccount.getUsername());
    }

    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void fullyAuthenticatedUserCannotBringForeignLogin() throws Exception {
        UserAccount userAccount = getUserFromBd(TestConstants.USER_ALICE);

        final String newLogin = TestConstants.USER_BOB;

        EditUserDTO edit = UserAccountConverter.convertToEditUserDto(userAccount);
        edit.setLogin(newLogin);

        MvcResult mvcResult = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.PROFILE)
                        .content(objectMapper.writeValueAsString(edit))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
        )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("user already present"))
                .andExpect(jsonPath("$.message").value("User with login 'bob' is already present"))
                .andReturn();

        LOGGER.info(mvcResult.getResponse().getContentAsString());
    }

    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void fullyAuthenticatedUserCannotBringForeignEmail() throws Exception {
        UserAccount userAccount = getUserFromBd(TestConstants.USER_ALICE);

        final String newEmail = TestConstants.USER_BOB+"@example.com";
        final Optional<UserAccount> foreignBobAccountOptional = userAccountRepository.findByEmail(newEmail);
        final UserAccount foreignBobAccount = foreignBobAccountOptional.orElseThrow(()->new RuntimeException("foreign email '"+newEmail+"' must be present"));
        final long foreingId = foreignBobAccount.getId();
        final String foreignPassword = foreignBobAccount.getPassword();
        final String foreignEmail = foreignBobAccount.getEmail();

        EditUserDTO edit = UserAccountConverter.convertToEditUserDto(userAccount);
        edit.setEmail(newEmail);

        MvcResult mvcResult = mockMvc.perform(
                post(Constants.Uls.API+Constants.Uls.PROFILE)
                        .content(objectMapper.writeValueAsString(edit))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
        )
                .andExpect(status().isOk()) // we care for emails
                .andReturn();

        LOGGER.info(mvcResult.getResponse().getContentAsString());

        UserAccount foreignAccountAfter = getUserFromBd(TestConstants.USER_BOB);
        Assert.assertEquals(foreingId, foreignAccountAfter.getId().longValue());
        Assert.assertEquals(foreignEmail, foreignAccountAfter.getEmail());
        Assert.assertEquals(foreignPassword, foreignAccountAfter.getPassword());

    }


    @Test
    @Ignore
    public void adminCanChangeAnyProfile() {

    }

    @Test
    @Ignore
    public void adminCanSeeAnybodyProfileEmail() {

    }

    /**
     * Alice see Bob's profile and she don't see his email
     * @throws Exception
     */
    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void userCannotSeeAnybodyProfileEmail() throws Exception {
        UserAccount bob = getUserFromBd(TestConstants.USER_BOB);
        String bobEmail = bob.getEmail();

        MvcResult mvcResult = mockMvc.perform(
                get(Constants.Uls.API+Constants.Uls.USER+"/"+bob.getId())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.login").value(TestConstants.USER_BOB))
                .andExpect(content().string(CoreMatchers.not(CoreMatchers.containsString(bobEmail))))
                .andReturn();

    }
    @Test
    @Ignore
    public void userCanSeeOnlyOwnProfileEmail() {

    }

}
