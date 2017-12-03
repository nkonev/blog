package com.github.nkonev.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.github.nkonev.CommonTestConstants;
import com.github.nkonev.FailoverUtils;
import com.github.nkonev.IntegrationTestConstants;
import com.github.nkonev.webdriver.configuration.SeleniumConfiguration;
import com.github.nkonev.integration.AbstractItTestRunner;
import com.github.nkonev.pages.object.Croppa;
import com.github.nkonev.pages.object.LoginModal;
import com.github.nkonev.pages.object.UserNav;
import com.github.nkonev.webdriver.selenium.Browser;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.github.nkonev.IntegrationTestConstants.Pages.USER;
import static com.github.nkonev.util.FileUtils.getExistsFile;

/**
 * Тест на страницу профиля
 * Created by nik on 06.06.17.
 */
public class UserProfileIT extends AbstractItTestRunner {

    @Value(IntegrationTestConstants.USER_ID)
    private int userId;

    @Autowired
    private SeleniumConfiguration seleniumConfiguration;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public static class UserProfilePage {
        private static final Logger LOGGER = LoggerFactory.getLogger(UserProfilePage.class);
        private String urlPrefix;
        private WebDriver driver;
        private static final int USER_PROFILE_WAIT = 20000;
        public UserProfilePage(String urlPrefix, WebDriver driver) {
            this.urlPrefix = urlPrefix;
            this.driver = driver;
        }
        public void openPage(int userId) {
            open(urlPrefix+USER+"/"+userId);
        }

        public void setAvatarImage(String absoluteFilePath) {
            Croppa.setImage(absoluteFilePath);
        }

        /**
         * Press Pencil
         */
        public void edit() {
            FailoverUtils.retry(2, () -> {
                $(".user-profile .manage-buttons img.edit-container-pen").click();
                $(".user-profile").waitUntil(Condition.text("Editing profile"), USER_PROFILE_WAIT);
                return null;
            });
        }

        public void assertThisIsYou() {
            $(".user-profile .user-profile-view-me .me").waitUntil(Condition.text("This is you"), USER_PROFILE_WAIT);
        }

        public String getAvatarUrl() {
            return $(".user-profile .avatar").getAttribute("src");
        }

        public void setLogin(String login) {
            $(".profile-edit-info input#login").setValue(login);
        }
        public void save(){
            $(".profile-edit button.save").click();
        }
        public void assertLogin(String expected){
            $(".user-profile .login").shouldHave(text((expected)));
        }
        public void assertMsg(long expectedId){
            $(".user-profile-view-msg").shouldHave(Condition.text("Viewing profile #" + expectedId));
        }
    }

    @Test
    public void userSeeThisIsYouAfterLogin() throws Exception {
        UserProfilePage userPage = new UserProfilePage(urlPrefix, driver);
        LoginModal loginModal = new LoginModal(user, password);

        FailoverUtils.retry(3, () -> {
            userPage.openPage(userId);
            loginModal.login();
            return null;
        });

        userPage.assertThisIsYou();

        Selenide.refresh();

        userPage.assertThisIsYou();
    }

    private long getUserAvatarCount() {
        return namedParameterJdbcTemplate.queryForObject("select count(*) from images.user_avatar_image;" , EmptySqlParameterSource.INSTANCE, Long.class);
    }


    @Test
    public void userEdit() throws Exception {
        Assume.assumeTrue(seleniumConfiguration.getBrowser() == Browser.CHROME || seleniumConfiguration.getBrowser() == Browser.FIREFOX);
        UserProfilePage userPage = new UserProfilePage(urlPrefix, driver);
        userPage.openPage(505);

        LoginModal loginModal = new LoginModal("generated_user_500", "generated_user_password");
        loginModal.login();

        userPage.assertThisIsYou();

        String urlOnPageBefore = userPage.getAvatarUrl();
        String urlInNavbarBefore = UserNav.getAvatarUrl();
        Assert.assertEquals(urlOnPageBefore, urlInNavbarBefore);

        userPage.edit();

        long countBefore = getUserAvatarCount();

        userPage.setAvatarImage(getExistsFile("../"+ CommonTestConstants.TEST_IMAGE, CommonTestConstants.TEST_IMAGE).getCanonicalPath());

        final String renamed = "generated_user_500_edit";
        userPage.setLogin(renamed);
        userPage.save();

        userPage.assertThisIsYou();
        userPage.assertLogin(renamed);

        long countAfter = getUserAvatarCount();
        Assert.assertEquals(countBefore+1, countAfter);

        String urlOnPageAfter = userPage.getAvatarUrl();
        String urlInNavbarAfter = UserNav.getAvatarUrl();
        Assert.assertFalse(StringUtils.isEmpty(urlOnPageAfter));
        Assert.assertNotEquals(urlOnPageBefore, urlOnPageAfter);
        Assert.assertEquals(urlOnPageAfter, urlInNavbarAfter);
    }


    @Test
    public void testUserProfileCorrectlyUpdatedWhenUrlSwitched() {
        UserListIT.UsersPage userPage = new UserListIT.UsersPage(urlPrefix);
        userPage.openPage();

        LoginModal loginModal = new LoginModal(user, password);
        loginModal.login();

        final long anotherUserId = 5;
        final String anotherUserLogin = "generated_user_0";

        UserProfilePage userProfilePage = new UserProfilePage(null, driver);

        FailoverUtils.retry(2, () -> {
            $(UserListIT.UsersPage.USERS_CONTAINER_SELECTOR)
                    .shouldHave(Condition.text(anotherUserLogin))
                    .findElementByLinkText(anotherUserLogin)
                    .click();

            userProfilePage.assertMsg(anotherUserId);
            return null;
        });

        userProfilePage.assertLogin(anotherUserLogin);
        String anotherUserAvatarUrl = userProfilePage.getAvatarUrl();

        UserNav.open();
        UserNav.profile();

        final long myUserId = 1;
        final String myLogin = user;

        userProfilePage.assertMsg(myUserId);
        userProfilePage.assertLogin(myLogin);
        String myAvatarUrl = userProfilePage.getAvatarUrl();

        Assert.assertNotEquals(anotherUserAvatarUrl, myAvatarUrl);
    }

}