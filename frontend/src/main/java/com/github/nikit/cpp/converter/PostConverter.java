package com.github.nikit.cpp.converter;

import com.github.nikit.cpp.dto.PostDTO;
import com.github.nikit.cpp.dto.PostDTOExtended;
import com.github.nikit.cpp.dto.PostDTOWithAuthorization;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.entity.jpa.Permissions;
import com.github.nikit.cpp.entity.jpa.Post;
import com.github.nikit.cpp.repo.jpa.PostRepository;
import com.github.nikit.cpp.security.BlogSecurityService;
import com.github.nikit.cpp.utils.XssSanitizeUtil;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
                blogSecurityService.hasPostPermission(saved, userAccount, Permissions.EDIT),
                blogSecurityService.hasPostPermission(saved, userAccount, Permissions.DELETE)
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
                blogSecurityService.hasPostPermission(saved, userAccount, Permissions.EDIT),
                blogSecurityService.hasPostPermission(saved, userAccount, Permissions.DELETE),
                left != null ? left.getId() : null,
                right != null ? right.getId() : null
        );
    }

    public Post convertToPost(PostDTO postDTO, Post forUpdate) {
        Assert.notNull(postDTO, "postDTO can't be null");

        if (forUpdate == null){ forUpdate = new Post(); }
        forUpdate.setText(XssSanitizeUtil.sanitize(postDTO.getText()));
        forUpdate.setTitle(postDTO.getTitle());
        forUpdate.setTitleImg(postDTO.getTitleImg());
        return forUpdate;
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
        return new PostDTO(post.getId(), post.getTitle(), cleanHtmlTags(post.getText()), post.getTitleImg() );
    }

}
