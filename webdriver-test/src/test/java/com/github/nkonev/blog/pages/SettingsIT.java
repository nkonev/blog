package com.github.nkonev.blog.pages;

import com.codeborne.selenide.Selenide;
import com.github.nkonev.blog.integration.AbstractItTestRunner;
import com.github.nkonev.blog.pages.object.IndexPage;
import com.github.nkonev.blog.pages.object.LoginModal;
import com.github.nkonev.blog.pages.object.SettingsPage;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;

public class SettingsIT extends AbstractItTestRunner {

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

        settingsPage.clickSave();

        settingsPage.waitForBackgroundImageWillSet();

        Assert.assertFalse($(SettingsPage.IMG_BG).getAttribute("src").isEmpty());

        Selenide.refresh();

        settingsPage.waitForBackgroundImageWillSet();

        Assert.assertFalse($(SettingsPage.IMG_BG).getAttribute("src").isEmpty());
    }


}