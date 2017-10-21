package com.github.nkonev.configuration;

import com.github.nkonev.IntegrationTestConstants;
import com.github.nkonev.selenium.Browser;
import com.github.nkonev.selenium.SeleniumFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Created by nik on 04.10.16.
 */
@Configuration
@ConfigurationProperties(prefix = "custom.selenium")
public class SeleniumConfigurationImpl implements SeleniumConfiguration {

    /**
     * in seconds
     */
    private int implicitlyWaitTimeout;

    private Browser browser;

    private int windowWidth;

    private int windowHeight;

    /**
     * in seconds
     */
    private int selenideConditionTimeout;

    /**
     * in seconds
     */
    private int selenideCollectionsTimeout;

    /**
     * @Scope("singleton") is need as part of https://github.com/spring-projects/spring-boot/issues/7454
     * @return
     * @throws Exception
     */
    @Scope("singleton")
    @Bean(initMethod = "start", destroyMethod = "stop")
    public SeleniumFactory seleniumComponent() throws Exception {
        return new SeleniumFactory(this);
    }

    public int getImplicitlyWaitTimeout() {
        return implicitlyWaitTimeout;
    }

    public void setImplicitlyWaitTimeout(int implicitlyWaitTimeout) {
        this.implicitlyWaitTimeout = implicitlyWaitTimeout;
    }

    public Browser getBrowser() {
        return browser;
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public int getSelenideConditionTimeout() {
        return selenideConditionTimeout;
    }

    public void setSelenideConditionTimeout(int selenideConditionTimeout) {
        this.selenideConditionTimeout = selenideConditionTimeout;
    }

    public int getSelenideCollectionsTimeout() {
        return selenideCollectionsTimeout;
    }

    public void setSelenideCollectionsTimeout(int selenideCollectionsTimeout) {
        this.selenideCollectionsTimeout = selenideCollectionsTimeout;
    }
}
