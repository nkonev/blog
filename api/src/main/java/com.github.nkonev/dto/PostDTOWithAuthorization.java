package com.github.nkonev.dto;

import java.time.LocalDateTime;
import java.util.Date;

public class PostDTOWithAuthorization extends PostDTO {
    private UserAccountDTO owner;
    private boolean canEdit;
    private boolean canDelete;
    private Date createDateTime;

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
        super(id, title, text, titleImg, createDateTime);
        this.owner = userAccountDTO;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }

    public PostDTOWithAuthorization() { }


    public UserAccountDTO getOwner() {
        return owner;
    }

    public void setOwner(UserAccountDTO owner) {
        this.owner = owner;
    }

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
