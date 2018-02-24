package com.github.nkonev.blog.dto;

import java.time.LocalDateTime;

public class PostDTOExtended extends PostDTOWithAuthorization {
    private Long left;
    private Long right;

    public PostDTOExtended() { }

    public PostDTOExtended(
            long id,
            String title,
            String text,
            String titleImg,
            UserAccountDTO userAccountDTO,
            boolean canEdit,
            boolean canDelete,
            Long left,
            Long right,
            LocalDateTime createDateTime
    ) {
        super(id, title, text, titleImg, userAccountDTO, canEdit, canDelete, createDateTime);
        this.left = left;
        this.right = right;
    }

    public Long getLeft() {
        return left;
    }

    public void setLeft(Long left) {
        this.left = left;
    }

    public Long getRight() {
        return right;
    }

    public void setRight(Long right) {
        this.right = right;
    }
}