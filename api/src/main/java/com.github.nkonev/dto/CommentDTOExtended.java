package com.github.nkonev.dto;

import java.time.LocalDateTime;

public class CommentDTOExtended extends CommentDTOWithAuthorization {
    private long commentsInPost;

    public CommentDTOExtended() { }

    public CommentDTOExtended(
            long id,
            String text,
            UserAccountDTO owner,
            boolean canEdit,
            boolean canDelete,
            long commentsInPost,
            LocalDateTime createDateTime
    ) {
        super(id, text, owner, canEdit, canDelete, createDateTime);
        this.commentsInPost = commentsInPost;
    }

    public long getCommentsInPost() {
        return commentsInPost;
    }

    public void setCommentsInPost(long commentsInPost) {
        this.commentsInPost = commentsInPost;
    }
}
