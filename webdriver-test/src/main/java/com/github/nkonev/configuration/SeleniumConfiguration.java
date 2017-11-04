package com.github.nkonev.configuration;

import com.github.nkonev.selenium.Browser;

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
