package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.PageUtils;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.entity.Post;
import com.github.nikit.cpp.entity.UserAccount;
import com.github.nikit.cpp.repo.PostRepository;
import com.github.nikit.cpp.repo.UserAccountRepository;
import org.jsoup.Jsoup;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
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
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

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

    @GetMapping(Constants.Uls.API_PUBLIC+Constants.Uls.POST)
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

    @GetMapping(Constants.Uls.API_PUBLIC+Constants.Uls.POST+"/{id}")
    public PostDTO getPost(
            @PathVariable("id") long id
    ) {
        return postRepository
                .findById(id)
                .map(PostController::convertToPostDTO)
                .orElseThrow(()-> new RuntimeException("Post " + id + " not found"));
    }


    // ================================================= secured

    @GetMapping(Constants.Uls.API+Constants.Uls.POST+Constants.Uls.MY)
    public List<PostDTO> getMyPosts(
            @RequestParam(value = "page", required=false, defaultValue = "0") int page,
            @RequestParam(value = "size", required=false, defaultValue = "0") int size,
            @RequestParam(value = "searchString", required=false, defaultValue = "") String searchString // TODO implement
    ) {
        page = PageUtils.fixPage(page);
        size = PageUtils.fixSize(size);

        PageRequest springDataPage = new PageRequest(page, size);

        return postRepository
                .findMyPosts(springDataPage).getContent()
                .stream()
                .map(PostController::convertToPostDTO)
                .collect(Collectors.toList());
    }

    @PostMapping(Constants.Uls.API+Constants.Uls.POST)
    public void createPost(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // null if not authenticated
            @RequestBody PostDTO postDTO) {
        postRepository.save(convertToPost(userAccount, postDTO));
    }

    @PutMapping(Constants.Uls.API+Constants.Uls.POST)
    public void updatePost(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount,
            @RequestBody PostDTO postDTO
    ) {
        postRepository.save(convertToPost(userAccount, postDTO));
    }

    private Post convertToPost(UserAccountDetailsDTO userAccountDetailsDTO, PostDTO postDTO) {
        if (userAccountDetailsDTO == null) { throw new IllegalArgumentException("userAccount can't be null"); }
        if (postDTO == null) { throw new IllegalArgumentException("postDTO can't be null"); }
        UserAccount ua = userAccountRepository.findOne(userAccountDetailsDTO.getId());
        Assert.notNull(ua, "User account not found");
        return new Post(postDTO.getId(), postDTO.getTitle(), postDTO.getText(), postDTO.getTitleImg(), ua);
    }

}
