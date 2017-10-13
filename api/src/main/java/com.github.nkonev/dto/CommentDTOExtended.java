package com.github.nkonev.dto;

public class CommentDTOExtended extends CommentDTOWithAuthorization {
    private long commentsInPost;

    public CommentDTOExtended() { }

    public CommentDTOExtended(long id, String text, UserAccountDTO owner, boolean canEdit, boolean canDelete, long commentsInPost) {
        super(id, text, owner, canEdit, canDelete);
        this.commentsInPost = commentsInPost;
    }

    public long getCommentsInPost() {
        return commentsInPost;
    }

    public void setCommentsInPost(long commentsInPost) {
        this.commentsInPost = commentsInPost;
    }
}
