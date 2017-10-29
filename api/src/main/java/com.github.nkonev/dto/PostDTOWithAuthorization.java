package com.github.nkonev.dto;

import java.time.LocalDateTime;

public class PostDTOWithAuthorization extends PostDTO {

    private boolean canEdit;
    private boolean canDelete;

    public PostDTOWithAuthorization(
            long id,
            String title,
            String text,
            String titleImg,
            UserAccountDTO userAccountDTO,
            boolean canEdit,
            boolean canDelete,
            LocalDateTime createDateTime
    ) {
        super(id, title, text, titleImg, createDateTime, userAccountDTO);
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }

    public PostDTOWithAuthorization() { }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }
}
