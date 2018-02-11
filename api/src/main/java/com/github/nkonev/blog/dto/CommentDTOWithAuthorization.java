package com.github.nkonev.blog.dto;

import java.time.LocalDateTime;

public class CommentDTOWithAuthorization extends CommentDTO {
    private UserAccountDTO owner;
    private boolean canEdit;
    private boolean canDelete;

    public CommentDTOWithAuthorization() { }

    public CommentDTOWithAuthorization(
            long id,
            String text,
            UserAccountDTO owner,
            boolean canEdit,
            boolean canDelete,
            LocalDateTime createDateTime
    ) {
        super(id, text, createDateTime);
        this.owner = owner;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }

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
