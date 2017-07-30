package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.PageUtils;
import com.github.nikit.cpp.converter.UserAccountConverter;
import com.github.nikit.cpp.dto.PostDTO;
import com.github.nikit.cpp.dto.PostDTOWithAuthorization;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.entity.Permissions;
import com.github.nikit.cpp.entity.Post;
import com.github.nikit.cpp.entity.UserAccount;
import com.github.nikit.cpp.exception.BadRequestException;
import com.github.nikit.cpp.repo.PostRepository;
import com.github.nikit.cpp.repo.UserAccountRepository;
import com.github.nikit.cpp.services.BlogPermissionEvaluator;
import org.jsoup.Jsoup;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
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

    @Autowired
    private BlogPermissionEvaluator blogPermissionEvaluator;

    // https://www.owasp.org/index.php/OWASP_Java_HTML_Sanitizer_Project
    private static final PolicyFactory SANITIZER_POLICY = new HtmlPolicyBuilder()
            .allowElements("a", "b", "img", "p")
            .allowUrlProtocols("https", "http")
            .allowAttributes("href").onElements("a")
            .allowAttributes("src").onElements("img")
            .requireRelNofollowOnLinks()
            .toFactory();

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
    public PostDTOWithAuthorization getPost(
            @PathVariable("id") long id,
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount // null if not authenticated
    ) {
        return postRepository
                .findById(id)
                .map(post -> convertToDto(post, userAccount))
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
    public PostDTOWithAuthorization createPost(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // null if not authenticated
            @RequestBody @NotNull PostDTO postDTO) {
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        if (postDTO.getId()!=0){
            throw new BadRequestException("cannot be setted");
        }
        Post fromWeb = convertToPost(postDTO, null);
        UserAccount ua = userAccountRepository.findOne(userAccount.getId()); // TODO check Hibernate cache for it
        Assert.notNull(ua, "User account not found");
        fromWeb.setOwner(ua);
        Post saved = postRepository.save(fromWeb);
        return convertToDto(saved, userAccount);
    }

    @PreAuthorize("hasPermission(#postDTO, T(com.github.nikit.cpp.entity.Permissions).EDIT)")
    @PutMapping(Constants.Uls.API+Constants.Uls.POST)
    public PostDTOWithAuthorization updatePost(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // null if not authenticated
            @RequestBody @NotNull PostDTO postDTO
    ) {
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        Post found = postRepository.findOne(postDTO.getId());
        Assert.notNull(found, "Post with id " + postDTO.getId() + " not found");
        Post updatedEntity = convertToPost(postDTO, found);
        Post saved = postRepository.save(updatedEntity);
        return convertToDto(saved, userAccount);
    }

    private PostDTOWithAuthorization convertToDto(Post saved, UserAccountDetailsDTO userAccount) {
        if (saved == null) { throw new IllegalArgumentException("Post can't be null"); }
        return new PostDTOWithAuthorization(
                saved.getId(),
                saved.getTitle(),
                saved.getText(),
                saved.getTitleImg(),
                UserAccountConverter.convertToUserAccountDTO(saved.getOwner()),
                blogPermissionEvaluator.hasPostPermission(saved, userAccount, Permissions.EDIT),
                blogPermissionEvaluator.hasPostPermission(saved, userAccount, Permissions.DELETE)
        );
    }

    private Post convertToPost(PostDTO postDTO, Post forUpdate) {
        if (postDTO == null) { throw new IllegalArgumentException("postDTO can't be null"); }
        if (forUpdate == null){ forUpdate = new Post(); }
        forUpdate.setText(postDTO.getText());
        forUpdate.setTitle(postDTO.getTitle());
        forUpdate.setTitleImg(postDTO.getTitleImg());
        return forUpdate;
    }

}
