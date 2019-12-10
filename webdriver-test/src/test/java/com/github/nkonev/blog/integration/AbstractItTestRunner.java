package com.github.nkonev.blog.integration;

/**
 * Created by nik on 27.05.17.
 */

import com.codeborne.selenide.Condition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nkonev.blog.CommonTestConstants;
import com.github.nkonev.blog.BlogApplication;
import com.github.nkonev.blog.webdriver.IntegrationTestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.io.IOException;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import static com.codeborne.selenide.Selenide.clearBrowserCookies;
import static com.github.nkonev.blog.util.FileUtils.getExistsFile;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {BlogApplication.class},
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

    @BeforeEach
    public void before() {
        LOGGER.debug("Executing before");
        clearBrowserCookies();
    }

    protected String getPencilImage() {
        try {
            return getExistsFile("../"+ CommonTestConstants.TEST_IMAGE, CommonTestConstants.TEST_IMAGE).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
