package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.PageUtils;
import com.github.nikit.cpp.converter.PostConverter;
import com.github.nikit.cpp.dto.PostDTO;
import com.github.nikit.cpp.dto.PostDTOWithAuthorization;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.entity.jpa.Post;
import com.github.nikit.cpp.entity.jpa.UserAccount;
import com.github.nikit.cpp.exception.BadRequestException;
import com.github.nikit.cpp.repo.jpa.PostRepository;
import com.github.nikit.cpp.repo.jpa.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@RestController
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PostConverter postConverter;

    @GetMapping(Constants.Uls.API+Constants.Uls.POST)
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
                .map(postConverter::convertToPostDTOWithCleanTags)
                .collect(Collectors.toList());
    }

    @GetMapping(Constants.Uls.API+Constants.Uls.POST+Constants.Uls.POST_ID)
    public PostDTOWithAuthorization getPost(
            @PathVariable(Constants.PathVariables.POST_ID) long id,
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount // null if not authenticated
    ) {
        return postRepository
                .findById(id)
                .map(post -> postConverter.convertToDto(post, userAccount))
                .orElseThrow(()-> new RuntimeException("Post " + id + " not found"));
    }


    // ================================================= secured

    @PreAuthorize("isAuthenticated()")
    @GetMapping(Constants.Uls.API+Constants.Uls.POST+Constants.Uls.MY)
    public List<PostDTO> getMyPosts(
            @RequestParam(value = "page", required=false, defaultValue = "0") int page,
            @RequestParam(value = "size", required=false, defaultValue = "0") int size,
            @RequestParam(value = "searchString", required=false, defaultValue = "") String searchString // TODO implement
    ) {

        PageRequest springDataPage = new PageRequest(PageUtils.fixPage(page), PageUtils.fixSize(size));

        return postRepository
                .findMyPosts(springDataPage).getContent()
                .stream()
                .map(postConverter::convertToPostDTOWithCleanTags)
                .collect(Collectors.toList());
    }

    // https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#el-common-built-in
    @PreAuthorize("isAuthenticated()")
    @PostMapping(Constants.Uls.API+Constants.Uls.POST)
    public PostDTOWithAuthorization addPost(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // null if not authenticated
            @RequestBody @NotNull PostDTO postDTO
    ) {
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        if (postDTO.getId()!=0){
            throw new BadRequestException("id cannot be set");
        }
        Post fromWeb = postConverter.convertToPost(postDTO, null);
        UserAccount ua = userAccountRepository.findOne(userAccount.getId()); // TODO check Hibernate cache for it
        Assert.notNull(ua, "User account not found");
        fromWeb.setOwner(ua);
        Post saved = postRepository.save(fromWeb);
        return postConverter.convertToDto(saved, userAccount);
    }

    @PreAuthorize("@blogSecurityService.hasPostPermission(#postDTO, #userAccount, T(com.github.nikit.cpp.entity.jpa.Permissions).EDIT)")
    @PutMapping(Constants.Uls.API+Constants.Uls.POST)
    public PostDTOWithAuthorization updatePost(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // null if not authenticated
            @RequestBody @NotNull PostDTO postDTO
    ) {
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        Post found = postRepository.findOne(postDTO.getId());
        Assert.notNull(found, "Post with id " + postDTO.getId() + " not found");
        Post updatedEntity = postConverter.convertToPost(postDTO, found);
        Post saved = postRepository.save(updatedEntity);
        return postConverter.convertToDto(saved, userAccount);
    }

    @PreAuthorize("@blogSecurityService.hasPostPermission(#postId, #userAccount, T(com.github.nikit.cpp.entity.jpa.Permissions).DELETE)")
    @DeleteMapping(Constants.Uls.API+Constants.Uls.POST+Constants.Uls.POST_ID)
    public void deletePost(
            @AuthenticationPrincipal UserAccountDetailsDTO userAccount, // null if not authenticated
            @PathVariable(Constants.PathVariables.POST_ID) long postId
    ) {
        Assert.notNull(userAccount, "UserAccountDetailsDTO can't be null");
        postRepository.delete(postId);
    }
}
