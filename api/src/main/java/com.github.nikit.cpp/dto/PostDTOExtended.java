package com.github.nikit.cpp.dto;

import java.net.URL;

public class PostDTOExtended extends PostDTOWithAuthorization {
    private Long left;
    private Long right;

    public PostDTOExtended() { }

    public PostDTOExtended(long id, String title, String text, URL titleImg, UserAccountDTO userAccountDTO, boolean canEdit, boolean canDelete, Long left, Long right) {
        super(id, title, text, titleImg, userAccountDTO, canEdit, canDelete);
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
