package com.github.nkonev.selenium;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.github.nkonev.configuration.SeleniumConfiguration;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.FactoryBean;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by nik on 07.08.16.
 */
public class SeleniumFactory implements FactoryBean<WebDriver> {

    public static final String PROPERTY_BROWSER_PHANTOM_LOG = "phantomjs.log";


    public static final String FIREFOX_DRIVER_VERSION = "0.18.0"; // https://github.com/mozilla/geckodriver/releases
    public static final String CHROME_DRIVER_VERSION = "2.33"; // https://sites.google.com/a/chromium.org/chromedriver/
    public static final String PHANTOM_JS_DRIVER_VERSION = "2.1.1"; // https://npm.taobao.org/mirrors/phantomjs

    private WebDriver driver;

    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumFactory.class);

    private SeleniumConfiguration seleniumConfiguration;

    public SeleniumFactory(SeleniumConfiguration seleniumConfiguration) throws Exception {
        this.seleniumConfiguration = seleniumConfiguration;
    }

    @Override
    public WebDriver getObject() throws Exception {
        return driver;
    }

    @Override
    public Class<WebDriver> getObjectType() {
        return WebDriver.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void start() {
        // http://www.slf4j.org/legacy.html#jul-to-slf4j
        // http://www.slf4j.org/api/org/slf4j/bridge/SLF4JBridgeHandler.html
        // Optionally remove existing handlers attached to j.u.l root logger
        SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.6.5)
        // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
        // the initialization phase of your application
        SLF4JBridgeHandler.install();

        switch(seleniumConfiguration.getBrowser()) {
            case PHANTOM:
            {
                PhantomJsDriverManager.getInstance().version(PHANTOM_JS_DRIVER_VERSION).setup(); // download executables if need and set System.properties

                String phantomLogProp = System.getProperty(PROPERTY_BROWSER_PHANTOM_LOG);
                File phantomLog = null;
                if (phantomLogProp != null){
                    phantomLog = new File(phantomLogProp);
                }
                String phantomjspath = System.getProperty(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY); // setted by PhantomJsDriverManager.getInstance().setup();
                PhantomJSDriverService phantomJSDriverService = new PhantomJSDriverService.Builder()
                        .usingPhantomJSExecutable(new File(phantomjspath))
                        .withLogFile(phantomLog)
                        .usingAnyFreePort()
                        .build();
                driver = new PhantomJSDriver(phantomJSDriverService, DesiredCapabilities.phantomjs());
            }
            break;
            case FIREFOX:
            {
                // firefox
                FirefoxDriverManager.getInstance().version(FIREFOX_DRIVER_VERSION).setup(); // download executables if need and set System.properties
                // https://developer.mozilla.org/en-US/Firefox/Headless_mode
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setLogLevel(Level.INFO);
                if (seleniumConfiguration.isHeadless()) {
                    firefoxOptions.addArguments("-headless");
                }
                driver = new FirefoxDriver(firefoxOptions);
                Configuration.browser = WebDriverRunner.MARIONETTE;
            }
            break;
            case CHROME:
            {
                ChromeDriverManager.getInstance().version(CHROME_DRIVER_VERSION).setup(); // download executables if need and set System.properties
                // https://developers.google.com/web/updates/2017/04/headless-chrome
                ChromeOptions chromeOptions = new ChromeOptions();
                if (seleniumConfiguration.isHeadless()) {
                    chromeOptions.addArguments("--headless");
                }
                driver = new ChromeDriver(chromeOptions);
            }
            break;
        }

        driver.manage().window().setSize(new Dimension(seleniumConfiguration.getWindowWidth(), seleniumConfiguration.getWindowHeight()));
        // http://www.seleniumhq.org/docs/04_webdriver_advanced.jsp#explicit-and-implicit-waits
        driver.manage().timeouts().implicitlyWait(seleniumConfiguration.getImplicitlyWaitTimeout(), TimeUnit.SECONDS); // wait for #findElement()

        // configure selenide
        WebDriverRunner.setWebDriver(driver);
        Configuration.timeout = seleniumConfiguration.getSelenideConditionTimeout() * 1000;
        Configuration.collectionsTimeout = seleniumConfiguration.getSelenideCollectionsTimeout() * 1000;
    }

    public void stop() {
        if (driver != null) {
            //Step 4- Close Driver
            driver.close();

            //Step 5- Quit Driver
            driver.quit();
        }

    }
}
