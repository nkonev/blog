package com.github.nikit.cpp.pages;

import com.codeborne.selenide.Condition;
import com.github.nikit.cpp.integration.AbstractItTestRunner;
import com.github.nikit.cpp.pages.object.IndexPage;
import com.github.nikit.cpp.pages.object.LoginModal;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;


public class PostIT extends AbstractItTestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostIT.class);

    public static class PostViewPage {
        public void assertTitle(String expected) {
            $(".post .post-head").shouldBe(CLICKABLE).should(Condition.have(Condition.text(expected)));
        }
        public void assertText(String expected) {
            $(".post .post-content").shouldBe(CLICKABLE).should(Condition.have(Condition.text(expected)));
        }
        public void edit() {
            $(".edit-container-pen").shouldBe(CLICKABLE).click();
        }
    }

    public static class PostEditPage {
        public void setTitle(String newTitle) {
            $("input.title").shouldBe(CLICKABLE).setValue(newTitle);
        }
        public void setText(String newText) {
            $("div.ql-editor").shouldBe(CLICKABLE).setValue(newText);
        }
        public void save() {
            $("button.save-btn").shouldBe(CLICKABLE).click(); // this can open login modal if you unauthenticated
        }
    }

    @Test
    public void addEditPost() throws Exception {
        LoginModal loginModal = new LoginModal(user, password);
        IndexPage indexPage = new IndexPage(urlPrefix);
        indexPage.openPage();

        PostEditPage postEditPage = new PostEditPage();
        PostViewPage postViewPage = new PostViewPage();

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
            postEditPage.save();
            postViewPage.assertText(newText);
            postViewPage.assertTitle(newTitle);
        }
    }

    private long getPostId(URL url) {
        String path = url.getPath();
        String[] splitted = path.split("/");
        return Long.valueOf(splitted[splitted.length-1]);
    }
}
