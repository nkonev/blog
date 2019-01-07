package com.github.nkonev.blog.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.github.nkonev.blog.FailoverUtils;
import com.github.nkonev.blog.integration.SocialEmulatorTests;
import com.github.nkonev.blog.pages.object.*;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import com.github.nkonev.blog.webdriver.IntegrationTestConstants;
import com.github.nkonev.blog.webdriver.configuration.SeleniumConfiguration;
import com.github.nkonev.blog.webdriver.selenium.Browser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;


public class PostIT extends SocialEmulatorTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostIT.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private SeleniumConfiguration seleniumConfiguration;

    private static final long POST_WITHOUT_COMMENTS = 90;
    private static final long POST_FOR_EDIT_COMMENTS = 80;

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
            $(".post .post-title").waitUntil(Condition.visible, 20 * 1000).should(Condition.text(expected));
        }

        public void assertText(String expected) {
            $(".post .post-content").waitUntil(Condition.visible, 20 * 1000).should(Condition.text(expected));
        }

        public void edit() {
            $(".post-head .edit-container-pen").shouldBe(CLICKABLE).click();
        }

        public void delete() {
            $(".post-head .remove-container-x")
                    .waitUntil(Condition.visible, 20 * 1000)
                    .waitUntil(Condition.enabled, 20 * 1000)
                    .shouldBe(CLICKABLE).click();
        }

        public void confirmDelete() {
            Dialog.waitForDialog();
            Dialog.clickYes();
        }
    }

    public static class PostEditPage {
        private WebDriver driver;
        public PostEditPage(WebDriver driver) {
            this.driver = driver;
        }
        public void setTitle(String newTitle) {
            $("input.title-edit").shouldBe(CLICKABLE).setValue(newTitle);
        }

        public void setText(String newText) {
            $("div.ql-editor")
                    .waitUntil(Condition.exist, 20 * 1000)
                    .waitUntil(Condition.visible, 20 * 1000)
                    .waitUntil(Condition.enabled, 20 * 1000)
                    .shouldBe(CLICKABLE).setValue(newText);
        }

        public void setTitleImage(String absoluteFilePath) {
            Croppa.setImage(absoluteFilePath);
        }

        public void save() {
            $(".post-command-buttons button.ok-btn")
                    .waitUntil(Condition.exist, 20 * 1000)
                    .waitUntil(Condition.visible, 20 * 1000)
                    .waitUntil(Condition.enabled, 20 * 1000)
                    .shouldBe(CLICKABLE).click(); // this can open login modal if you unauthenticated
        }
    }

    public static class CommentEdit {
        private static final String COMMENT_LIST_SELECTOR = ".comments #comments-list";
        private static final String COMMENT_EDIT_TEXTAREA_SELECTOR = ".comment-edit textarea";

        public void addComment(String text) {
            FailoverUtils.retry(2, () -> {
                final int waitSec = 10;
                $(COMMENT_EDIT_TEXTAREA_SELECTOR)
                        .waitUntil(Condition.exist, waitSec * 1000)
                        .waitUntil(Condition.visible, waitSec * 1000)
                        .waitUntil(Condition.enabled, waitSec * 1000)
                        .scrollTo()
                        .setValue(text);
                $(".comment-command-buttons .ok-btn")
                        .waitUntil(Condition.exist, waitSec * 1000)
                        .waitUntil(Condition.visible, waitSec * 1000)
                        .waitUntil(Condition.enabled, waitSec * 1000)
                        .scrollTo()
                        .click();

                $(COMMENT_LIST_SELECTOR).shouldHave(Condition.text(text));
                return null;
            });
        }

        public void checkPaginator(int expected){
            $("div.comments li.active").shouldHave(Condition.text(""+expected));
        }

        private SelenideElement findComment(int index) {
            return $$(COMMENT_LIST_SELECTOR).get(index);
        }

        /**
         *
         * @param index order index
         */
        public void editComment(int index, String newText){
            SelenideElement comment = findComment(index);
            comment.find(".comment-manage-buttons .edit-container-pen").click();
            comment.find(COMMENT_EDIT_TEXTAREA_SELECTOR).setValue(newText);
            comment.find(".comment-command-buttons .ok-btn").click();

            $(COMMENT_LIST_SELECTOR).shouldHave(Condition.text(newText));
        }

        /**
         *
         * @param index order index
         */
        public void deleteComment(int index, String deletedText){
            SelenideElement comment = findComment(index);
            comment.find(".comment-manage-buttons .remove-container-x").click();

            Dialog.waitForDialog();
            Dialog.clickYes();

            $(COMMENT_LIST_SELECTOR).waitUntil(Condition.not(Condition.text(deletedText)), 7 * 1000, 1000);
        }
    }

    private int getCommentPage() throws MalformedURLException {
        return Integer.valueOf(new URL(driver.getCurrentUrl()).getQuery().split("=")[1]);
    }

    @org.junit.jupiter.api.Test
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

            FailoverUtils.retry(2, () -> {
                postEditPage.save();

                // TimeUnit.SECONDS.sleep(3);
                postViewPage.assertText(text);
                postViewPage.assertTitle(title);
                return null;
            });

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

            if (seleniumConfiguration.getBrowser()== Browser.CHROME || seleniumConfiguration.getBrowser()==Browser.FIREFOX) {
                postEditPage.setTitleImage(getPencilImage());
            }
            postEditPage.save();

            if (seleniumConfiguration.getBrowser()==Browser.CHROME || seleniumConfiguration.getBrowser()==Browser.FIREFOX) {
                FailoverUtils.assertPoll(() -> !StringUtils.isEmpty(postRepository.findById(postId).orElseThrow(()->new RuntimeException("not found test post by id")).getTitleImg()), 15);
            }

            postViewPage.assertText(newText);
            postViewPage.assertTitle(newTitle);
        }
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

        String urlBefore = driver.getCurrentUrl();

        FailoverUtils.retry(3, () -> {
            postViewPage.delete();
            postViewPage.confirmDelete();
            String urlAfter = driver.getCurrentUrl();
            Assertions.assertNotEquals(urlBefore, urlAfter);
            return null;
        });

        FailoverUtils.assertPoll(()-> !postRepository.findById(id).isPresent(), 15);
        FailoverUtils.assertPoll(()-> !deletablePageUrl.equals(driver.getCurrentUrl()), 10);
    }

    @org.junit.jupiter.api.Test
    public void testAddComments() throws MalformedURLException {
        PostViewPage postViewPage = new PostViewPage(urlPrefix);
        postViewPage.openPost(POST_WITHOUT_COMMENTS);

        LoginModal loginModal = new LoginModal(user, password);
        loginModal.openLoginModal();
        loginModal.login();

        CommentEdit commentEdit = new CommentEdit();

        for(int i=1; i<=10; ++i) {
            final int page = 1;
            commentEdit.addComment("comment " + i);
            final int commentPage = getCommentPage();
            Assertions.assertEquals(page, commentPage);
            commentEdit.checkPaginator(page);
        }

        for(int i=11; i<=20; ++i) {
            final int page = 2;
            commentEdit.addComment("comment " + i);
            final int commentPage = getCommentPage();
            Assertions.assertEquals(page, commentPage);
            commentEdit.checkPaginator(page);
        }
    }

    @org.junit.jupiter.api.Test
    public void testAddEditAndDeleteComments() throws MalformedURLException {
        PostViewPage postViewPage = new PostViewPage(urlPrefix);
        postViewPage.openPost(POST_FOR_EDIT_COMMENTS);

        LoginModal loginModal = new LoginModal(user, password);
        loginModal.openLoginModal();
        loginModal.login();

        CommentEdit commentEdit = new CommentEdit();

        final int page = 1;
        commentEdit.addComment("comment for edit and delete");
        int commentPage = getCommentPage();
        Assertions.assertEquals(page, commentPage);
        commentEdit.checkPaginator(page);

        final String newCommentText = "new comment text";
        commentEdit.editComment(0, newCommentText);

        commentEdit.deleteComment(0, newCommentText);
    }


    @Test
    public void facebookLoginFromPostPageReturnsToPostPage() throws Exception {
        PostViewPage postViewPage = new PostViewPage(urlPrefix);
        postViewPage.openPost(POST_FOR_EDIT_COMMENTS);

        LoginModal loginModal = new LoginModal();
        loginModal.openLoginModal();
        loginModal.loginFacebook();

        Assertions.assertEquals(facebookLogin, UserNav.getLogin());

        long postId = getPostId(URI.create(driver.getCurrentUrl()).toURL());
        Assertions.assertEquals(POST_FOR_EDIT_COMMENTS, postId);
    }

}