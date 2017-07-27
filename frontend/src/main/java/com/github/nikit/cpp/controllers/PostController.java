package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.PageUtils;
import com.github.nikit.cpp.entity.Post;
import com.github.nikit.cpp.repo.PostRepository;
import org.jsoup.Jsoup;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * document.getElementsByTagName('body')[0].innerHTML = 'XSSed';
 */

@RestController
@RequestMapping(Constants.Uls.API_PUBLIC+Constants.Uls.POST)
public class PostController {

    @Autowired
    private PostRepository postRepository;

    // https://www.owasp.org/index.php/OWASP_Java_HTML_Sanitizer_Project
    private static final PolicyFactory SANITIZER_POLICY = new HtmlPolicyBuilder()
            .allowElements("a", "b", "img", "p")
            .allowUrlProtocols("https", "http")
            .allowAttributes("href").onElements("a")
            .allowAttributes("src").onElements("img")
            .requireRelNofollowOnLinks()
            .toFactory();

    public static class PostDTO {
        private long id;
        private String title;
        private String text;
        private URL titleImg;

        public PostDTO() { }

        public PostDTO(long id, String title, String text, URL titleImg) {
            this.id = id;
            this.title = title;
            this.text = text;
            this.titleImg = titleImg;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public URL getTitleImg() {
            return titleImg;
        }

        public void setTitleImg(URL titleImg) {
            this.titleImg = titleImg;
        }
    }

    private static String cleanHtmlTags(String html) {
        return html == null ? null : Jsoup.parse(html).text();
    }

    public static PostDTO convertToPostDTO(Post post) {
        if (post==null) {return null;}
        return new PostDTO(post.getId(), post.getTitle(), cleanHtmlTags(post.getText()), post.getTitleImg() );
    }

    @GetMapping
    public List<PostDTO> getPosts(
            @RequestParam(value = "page", required=false, defaultValue = "0") int page,
            @RequestParam(value = "size", required=false, defaultValue = "0") int size,
            @RequestParam(value = "searchString", required=false, defaultValue = "") String searchString
    ) {
        page = PageUtils.fixPage(page);
        size = PageUtils.fixSize(size);

        PageRequest springDataPage = new PageRequest(page, size);

        return postRepository
                .findByTextContainsOrTitleContainsOrderByIdDesc(springDataPage, searchString, searchString).getContent()
                .stream()
                .map(PostController::convertToPostDTO)
                .collect(Collectors.toList());
    }

    public static final String sanitize(String html) {
        return SANITIZER_POLICY.sanitize(html);
    }

    @GetMapping("/{id}")
    public PostDTO getPost(
            @PathVariable("id") long id
    ) {
        return postRepository
                .findById(id)
                .map(PostController::convertToPostDTO)
                .orElseThrow(()-> new RuntimeException("Post " + id + " not found"));
    }

}
