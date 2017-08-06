package com.github.nikit.cpp.converter;

import com.github.nikit.cpp.dto.CommentDTO;
import com.github.nikit.cpp.dto.CommentDTOWithAuthorization;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.entity.jpa.Comment;
import com.github.nikit.cpp.entity.jpa.Permissions;
import com.github.nikit.cpp.security.BlogSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class CommentConverter {

    @Autowired
    private BlogSecurityService blogSecurityService;

    public Comment convertFromDto(CommentDTO commentDTO, long postId, Comment forUpdate) {
        Assert.notNull(commentDTO, "commentDTO can't be null");
        if (forUpdate == null) {
            forUpdate = new Comment();
            forUpdate.setPostId(postId);
        }
        forUpdate.setText(commentDTO.getText());
        return forUpdate;
    }

    public CommentDTOWithAuthorization convertToDto(Comment comment, UserAccountDetailsDTO userAccount) {
        Assert.notNull(comment, "comment can't be null");

        return new CommentDTOWithAuthorization(
                comment.getId(),
                comment.getText(),
                UserAccountConverter.convertToUserAccountDTO(comment.getOwner()),
                blogSecurityService.hasCommentPermission(comment, userAccount, Permissions.EDIT),
                blogSecurityService.hasCommentPermission(comment, userAccount, Permissions.DELETE)
        );
    }

    public CommentDTO convertToDto(Comment comment) {
        Assert.notNull(comment, "comment can't be null");

        return new CommentDTO(
                comment.getId(),
                comment.getText()
        );

    }
}
