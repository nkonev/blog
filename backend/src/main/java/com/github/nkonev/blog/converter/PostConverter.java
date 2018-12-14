package com.github.nkonev.blog.converter;

import com.github.nkonev.blog.dto.*;
import com.github.nkonev.blog.entity.elasticsearch.IndexPost;
import com.github.nkonev.blog.entity.jpa.Post;
import com.github.nkonev.blog.exception.BadRequestException;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import com.github.nkonev.blog.security.BlogSecurityService;
import com.github.nkonev.blog.security.permissions.PostPermissions;
import com.github.nkonev.blog.services.XssSanitizerService;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Service
public class PostConverter {
    @Autowired
    private BlogSecurityService blogSecurityService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserAccountConverter userAccountConverter;

    @Autowired
    private XssSanitizerService xssSanitizerService;

    public PostDTOWithAuthorization convertToDto(Post saved, UserAccountDetailsDTO userAccount) {
        Assert.notNull(saved, "Post can't be null");

        return new PostDTOWithAuthorization(
                saved.getId(),
                saved.getTitle(),
                (saved.getText()),
                saved.getTitleImg(),
                userAccountConverter.convertToUserAccountDTO(saved.getOwner()),
                blogSecurityService.hasPostPermission(saved, userAccount, PostPermissions.EDIT),
                blogSecurityService.hasPostPermission(saved, userAccount, PostPermissions.DELETE),
                saved.getCreateDateTime()
        );
    }

    public PostDTOExtended convertToDtoExtended(Post saved, UserAccountDetailsDTO userAccount) {
        Assert.notNull(saved, "Post can't be null");

        Post left = postRepository.getLeft(saved.getId());
        Post right = postRepository.getRight(saved.getId());

        return new PostDTOExtended(
                saved.getId(),
                saved.getTitle(),
                (saved.getText()),
                saved.getTitleImg(),
                userAccountConverter.convertToUserAccountDTO(saved.getOwner()),
                blogSecurityService.hasPostPermission(saved, userAccount, PostPermissions.EDIT),
                blogSecurityService.hasPostPermission(saved, userAccount, PostPermissions.DELETE),
                left != null ? new PostPreview(left.getId(), left.getTitle()) : null,
                right != null ? new PostPreview(right.getId(), right.getTitle()) : null,
                saved.getCreateDateTime()
        );
    }

    private void checkLength(String comment) {
        final int MIN_POST_LENGTH = 10;
        String trimmed = StringUtils.trimWhitespace(comment);
        if (trimmed == null || trimmed.length() < MIN_POST_LENGTH) {
            throw new BadRequestException("post is too short, must be longer than " + MIN_POST_LENGTH);
        }
    }

    public Post convertToPost(PostDTO postDTO, Post forUpdate) {
        Assert.notNull(postDTO, "postDTO can't be null");

        if (forUpdate == null){ forUpdate = new Post(); }
        String sanitizedHtml = xssSanitizerService.sanitize(postDTO.getText());
        checkLength(sanitizedHtml);
        forUpdate.setText(sanitizedHtml);
        forUpdate.setTitle(cleanHtmlTags(postDTO.getTitle()));
        if (Boolean.TRUE.equals(postDTO.getRemoveTitleImage())) {
            forUpdate.setTitleImg(null);
        } else {
            forUpdate.setTitleImg(postDTO.getTitleImg());
        }
        return forUpdate;
    }

    public IndexPost toElasticsearchPost(com.github.nkonev.blog.entity.jpa.Post jpaPost) {
        String sanitizedHtml = xssSanitizerService.sanitize(jpaPost.getText());
        return new IndexPost(jpaPost.getId(), jpaPost.getTitle(), cleanHtmlTags(sanitizedHtml));
    }

    public static void cleanTags(PostDTO postDTO) {
        postDTO.setText(cleanHtmlTags(postDTO.getText()));;
    }

    /**
     * Used in main page
     * @param html
     * @return
     */
    private static String cleanHtmlTags(String html) {
        return html == null ? null : Jsoup.parse(html).text();
    }

    /**
     * Used in main page
     */
    public PostDTO convertToPostDTO(Post post) {
        if (post==null) {return null;}

        return new PostDTO(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getTitleImg(),
                post.getCreateDateTime(),
                userAccountConverter.convertToUserAccountDTO(post.getOwner())
        );
    }

}
