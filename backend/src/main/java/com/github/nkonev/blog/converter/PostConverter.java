package com.github.nkonev.blog.converter;

import com.github.nkonev.blog.dto.PostDTO;
import com.github.nkonev.blog.dto.PostDTOExtended;
import com.github.nkonev.blog.dto.PostDTOWithAuthorization;
import com.github.nkonev.blog.dto.UserAccountDetailsDTO;
import com.github.nkonev.blog.entity.jpa.Post;
import com.github.nkonev.blog.exception.BadRequestException;
import com.github.nkonev.blog.exception.DataNotFoundException;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import com.github.nkonev.blog.security.BlogSecurityService;
import com.github.nkonev.blog.security.permissions.PostPermissions;
import com.github.nkonev.blog.utils.XssSanitizeUtil;
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

    public PostDTOWithAuthorization convertToDto(Post saved, UserAccountDetailsDTO userAccount) {
        Assert.notNull(saved, "Post can't be null");

        return new PostDTOWithAuthorization(
                saved.getId(),
                saved.getTitle(),
                (saved.getText()),
                saved.getTitleImg(),
                UserAccountConverter.convertToUserAccountDTO(saved.getOwner()),
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
                UserAccountConverter.convertToUserAccountDTO(saved.getOwner()),
                blogSecurityService.hasPostPermission(saved, userAccount, PostPermissions.EDIT),
                blogSecurityService.hasPostPermission(saved, userAccount, PostPermissions.DELETE),
                left != null ? left.getId() : null,
                right != null ? right.getId() : null,
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
        String sanitizedHtml = XssSanitizeUtil.sanitize(postDTO.getText());
        checkLength(sanitizedHtml);
        forUpdate.setText(sanitizedHtml);
        forUpdate.setTitle(cleanHtmlTags(postDTO.getTitle()));
        forUpdate.setTitleImg(postDTO.getTitleImg());

        return forUpdate;
    }

    public static com.github.nkonev.blog.entity.elasticsearch.Post toElasticsearchPost(com.github.nkonev.blog.entity.jpa.Post jpaPost) {
        String sanitizedHtml = XssSanitizeUtil.sanitize(jpaPost.getText());
        return new com.github.nkonev.blog.entity.elasticsearch.Post(jpaPost.getId(), jpaPost.getTitle(), cleanHtmlTags(sanitizedHtml));
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
    public PostDTO convertToPostDTOWithCleanTags(Post post) {
        if (post==null) {return null;}

        return new PostDTO(
                post.getId(),
                post.getTitle(),
                null,
                post.getTitleImg(),
                post.getCreateDateTime(),
                UserAccountConverter.convertToUserAccountDTO(post.getOwner())
        );
    }

}
