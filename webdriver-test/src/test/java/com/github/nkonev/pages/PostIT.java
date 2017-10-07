package com.github.nkonev.pages;

import com.codeborne.selenide.Condition;
import com.github.nkonev.CommonTestConstants;
import com.github.nkonev.IntegrationTestConstants;
import com.github.nkonev.configuration.SeleniumConfiguration;
import com.github.nkonev.integration.AbstractItTestRunner;
import com.github.nkonev.pages.object.IndexPage;
import com.github.nkonev.pages.object.LoginModal;
import com.github.nkonev.repo.jpa.PostRepository;
import com.github.nkonev.selenium.Browser;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;


public class PostIT extends AbstractItTestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostIT.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private SeleniumConfiguration seleniumConfiguration;

    public static class PostViewPage {
        private static final String POST_PART = "/post/";
        private String urlPrefix;

        public PostViewPage(String urlPrefix) {
            this.urlPrefix = urlPrefix;
        }

        public void openPost(long id) {
            open(getUrl(id));
        }

        public String getUrl(long id) {
            return urlPrefix+POST_PART+id;
        }

        public void assertTitle(String expected) {
            $(".post .post-head").waitUntil(Condition.visible, 20 * 1000).should(Condition.text(expected));
        }

        public void assertText(String expected) {
            $(".post .post-content").waitUntil(Condition.visible, 20 * 1000).should(Condition.text(expected));
        }

        public void edit() {
            $(".edit-container-pen").shouldBe(CLICKABLE).click();
        }

        public void delete() {
            $(".remove-container-x")
                    .waitUntil(Condition.visible, 20 * 1000)
                    .waitUntil(Condition.enabled, 20 * 1000)
                    .shouldBe(CLICKABLE).click();
        }
    }

    public static class PostEditPage {
        private WebDriver driver;
        public PostEditPage(WebDriver driver) {
            this.driver = driver;
        }
        public void setTitle(String newTitle) {
            $("input.title").shouldBe(CLICKABLE).setValue(newTitle);
        }

        public void setText(String newText) {
            $("div.ql-editor")
                    .waitUntil(Condition.exist, 20 * 1000)
                    .waitUntil(Condition.visible, 20 * 1000)
                    .waitUntil(Condition.enabled, 20 * 1000)
                    .shouldBe(CLICKABLE).setValue(newText);
        }

        public void setTitleImage(String absoluteFilePath) {
            final By croppaId = By.cssSelector(".croppa-container input");
            final Wait<WebDriver> wait = new FluentWait<WebDriver>(driver).withMessage("croppa upload element was not found").withTimeout(10, TimeUnit.SECONDS).pollingEvery(1, TimeUnit.SECONDS);
            wait.until(webDriver -> ExpectedConditions.visibilityOf(driver.findElement(croppaId)));
            driver.findElement(croppaId).sendKeys(absoluteFilePath);
        }

        public void save() {
            $("button.save-btn")
                    .waitUntil(Condition.exist, 20 * 1000)
                    .waitUntil(Condition.visible, 20 * 1000)
                    .waitUntil(Condition.enabled, 20 * 1000)
                    .shouldBe(CLICKABLE).click(); // this can open login modal if you unauthenticated
        }
    }

    @Test
    public void addEditPost() throws Exception {
        LoginModal loginModal = new LoginModal(user, password);
        IndexPage indexPage = new IndexPage(urlPrefix);
        indexPage.openPage();

        PostEditPage postEditPage = new PostEditPage(driver);
        PostViewPage postViewPage = new PostViewPage(urlPrefix);

        // add post
        final long postId;
        final URL createdPost;
        {
            indexPage.clickAddPost();

            final String title = "autotest post";
            final String text = "New post created from autotest with love";

            postEditPage.setText(text);
            postEditPage.setTitle(title);
            postEditPage.save();

            loginModal.login();

            postEditPage.save();

            // TimeUnit.SECONDS.sleep(3);
            postViewPage.assertText(text);
            postViewPage.assertTitle(title);

            createdPost = new URL(driver.getCurrentUrl());
            postId = getPostId(createdPost);
            LOGGER.info("Post successfully created, its url: {}, postId: {}", createdPost, postId);
        }

        // edit post
        {
            open(createdPost);
            postViewPage.edit();

            final String newTitle = "autotest edited title";
            final String newText = "New post edited from autotest with love";
            postEditPage.setText(newText);
            postEditPage.setTitle(newTitle);

            if (seleniumConfiguration.getBrowser()==Browser.CHROME || seleniumConfiguration.getBrowser()==Browser.FIREFOX) {
                postEditPage.setTitleImage(getExistsFile("../"+CommonTestConstants.TEST_IMAGE, CommonTestConstants.TEST_IMAGE).getCanonicalPath());
            }
            postEditPage.save();

            if (seleniumConfiguration.getBrowser()==Browser.CHROME || seleniumConfiguration.getBrowser()==Browser.FIREFOX) {
                assertPoll(() -> !StringUtils.isEmpty(postRepository.findOne(postId).getTitleImg()), 15);
            }

            postViewPage.assertText(newText);
            postViewPage.assertTitle(newTitle);
        }
    }

    private File getExistsFile(String... fileCandidates) {
        for(String fileCandidate: fileCandidates) {
            File file = new File(fileCandidate);
            if (file.exists()) {
                return file;
            }
        }
        throw new RuntimeException("exists file not found among " + Arrays.toString(fileCandidates));
    }

    private long getPostId(URL url) {
        String path = url.getPath();
        String[] splitted = path.split("/");
        return Long.valueOf(splitted[splitted.length - 1]);
    }

    @Test
    public void deletePostWithComments() throws Exception {
        LoginModal loginModal = new LoginModal(user, password);
        PostViewPage postViewPage = new PostViewPage(urlPrefix);
        final long id = IntegrationTestConstants.POST_WITH_COMMENTS;
        postViewPage.openPost(id);
        String deletablePageUrl = postViewPage.getUrl(id);

        loginModal.openLoginModal();
        loginModal.login();

        postViewPage.delete();

        assertPoll(()-> !postRepository.findById(id).isPresent(), 15);
        assertPoll(()-> !deletablePageUrl.equals(driver.getCurrentUrl()), 10);
    }

}