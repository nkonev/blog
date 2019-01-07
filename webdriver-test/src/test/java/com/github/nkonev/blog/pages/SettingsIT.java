package com.github.nkonev.blog.pages;

import com.codeborne.selenide.Selenide;
import com.github.nkonev.blog.FailoverUtils;
import com.github.nkonev.blog.integration.AbstractItTestRunner;
import com.github.nkonev.blog.pages.object.IndexPage;
import com.github.nkonev.blog.pages.object.LoginModal;
import com.github.nkonev.blog.pages.object.SettingsPage;
import com.github.nkonev.blog.repo.jpa.RuntimeSettingsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.codeborne.selenide.Selenide.$;
import static com.github.nkonev.blog.controllers.SettingsController.IMAGE_BACKGROUND;

public class SettingsIT extends AbstractItTestRunner {

    @Autowired
    private RuntimeSettingsRepository runtimeSettingsRepository;

    @Test
    public void testSetImage() throws Exception {
        IndexPage indexPage = new IndexPage(urlPrefix);
        indexPage.openPage();

        LoginModal loginModal = new LoginModal(user, password);
        loginModal.openLoginModal();
        loginModal.login();

        indexPage.clickSettings();

        SettingsPage settingsPage = new SettingsPage(urlPrefix);
        settingsPage.setBackgroundImage(getPencilImage());
        settingsPage.waitForBackgroundImageWillSet();

        FailoverUtils.retry(3, () -> {
            settingsPage.clickSave();
            Assertions.assertFalse($(SettingsPage.IMG_BG).getAttribute("src").isEmpty());

            runtimeSettingsRepository.findById(IMAGE_BACKGROUND).orElseThrow();
            return null;
        });


        FailoverUtils.retry(3, () -> {
            Selenide.refresh();

            settingsPage.waitForBackgroundImageWillSet();

            Assertions.assertFalse($(SettingsPage.IMG_BG).getAttribute("src").isEmpty());
            return null;
        });
    }


}