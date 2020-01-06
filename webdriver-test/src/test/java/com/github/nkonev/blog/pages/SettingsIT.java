package com.github.nkonev.blog.pages;

import com.github.nkonev.blog.FailoverUtils;
import com.github.nkonev.blog.entity.jdbc.RuntimeSettings;
import com.github.nkonev.blog.integration.AbstractItTestRunner;
import com.github.nkonev.blog.pages.object.IndexPage;
import com.github.nkonev.blog.pages.object.LoginModal;
import com.github.nkonev.blog.pages.object.SettingsPage;
import com.github.nkonev.blog.repo.jdbc.RuntimeSettingsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static com.codeborne.selenide.Selenide.$;
import static com.github.nkonev.blog.controllers.SettingsController.IMAGE_BACKGROUND;

public class SettingsIT extends AbstractItTestRunner {

    @Autowired
    private RuntimeSettingsRepository runtimeSettingsRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsIT.class);

    @Test
    public void testSetImage() throws Exception {
        IndexPage indexPage = new IndexPage(urlPrefix);
        indexPage.openPage();

        LoginModal loginModal = new LoginModal(user, password);
        loginModal.openLoginModal();
        loginModal.login();

        indexPage.clickSettings();

        SettingsPage settingsPage = new SettingsPage(urlPrefix);

        FailoverUtils.retry(3, () -> {
            settingsPage.setBackgroundImage(getPencilImage());
            settingsPage.waitForBackgroundImageWillSet();

            settingsPage.clickSave();
            Assertions.assertFalse($(SettingsPage.IMG_BG).getAttribute("src").isEmpty());

            Optional<RuntimeSettings> byId = runtimeSettingsRepository.findById(IMAGE_BACKGROUND);
            if (byId.isEmpty()) {
                LOGGER.error("We didn't found {} in database", IMAGE_BACKGROUND);
                throw new RuntimeException();
            }
            return null;
        });

    }

}