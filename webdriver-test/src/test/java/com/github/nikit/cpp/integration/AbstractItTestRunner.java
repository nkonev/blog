package com.github.nikit.cpp.integration;

/**
 * Created by nik on 27.05.17.
 */

import com.codeborne.selenide.Condition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nikit.cpp.IntegrationTestConstants;
import com.github.nikit.cpp.Launcher;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static com.codeborne.selenide.Selenide.clearBrowserCookies;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(
        classes = {Launcher.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
public abstract class AbstractItTestRunner {

    // http://www.seleniumhq.org/docs/04_webdriver_advanced.jsp#expected-conditions
    // clickable https://seleniumhq.github.io/selenium/docs/api/java/org/openqa/selenium/support/ui/ExpectedConditions.html#elementToBeClickable-org.openqa.selenium.By-
    public static final Condition[] CLICKABLE = {Condition.exist, Condition.enabled, Condition.visible};

    @Value(IntegrationTestConstants.URL_PREFIX)
    protected String urlPrefix;

    @Value(IntegrationTestConstants.USER)
    protected String user;

    @Value(IntegrationTestConstants.PASSWORD)
    protected String password;

    @Autowired
    protected WebDriver driver;

    private Logger LOGGER = LoggerFactory.getLogger(AbstractItTestRunner.class);

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Before
    public void before() {
        LOGGER.debug("Executing before");
        clearBrowserCookies();
    }
}
