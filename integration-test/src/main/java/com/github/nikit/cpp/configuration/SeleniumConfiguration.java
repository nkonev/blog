package com.github.nikit.cpp.configuration;

import com.github.nikit.cpp.selenium.Browser;

/**
 * Created by nik on 15.07.17.
 */
public interface SeleniumConfiguration {
    int getImplicitlyWaitTimeout();
    Browser getBrowser();
    int getWindowWidth();
    int getWindowHeight();
}
