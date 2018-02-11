package com.github.nkonev.blog.webdriver.configuration;

import com.github.nkonev.blog.webdriver.selenium.Browser;

/**
 * Created by nik on 15.07.17.
 */
public interface SeleniumConfiguration {
    int getImplicitlyWaitTimeout();
    Browser getBrowser();
    int getWindowWidth();
    int getWindowHeight();
    int getSelenideConditionTimeout();
    int getSelenideCollectionsTimeout();
    boolean isHeadless();
}
