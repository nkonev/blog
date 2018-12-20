package com.github.nkonev.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.nkonev.blog.ApiConstants;

import java.time.LocalDateTime;

public class PostDTO {
    private long id;
    private String title;
    private String text;
    private String titleImg;
    private UserAccountDTO owner;
    private Integer commentCount;
    private Boolean removeTitleImage;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= ApiConstants.DATE_FORMAT)
    private LocalDateTime createDateTime;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= ApiConstants.DATE_FORMAT)
    private LocalDateTime editDateTime;

    public PostDTO() { }

    public PostDTO(long id, String title, String text, String titleImg, LocalDateTime createDateTime, LocalDateTime editDateTime, Integer commentCount, UserAccountDTO owner) {
        this(id, title, text, titleImg, createDateTime, editDateTime, owner);
        this.commentCount = commentCount;
    }

    public PostDTO(long id, String title, String text, String titleImg, LocalDateTime createDateTime, LocalDateTime editDateTime, UserAccountDTO owner) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.titleImg = titleImg;
        this.createDateTime = createDateTime;
        this.editDateTime = editDateTime;
        this.owner = owner;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Boolean getRemoveTitleImage() {
        return removeTitleImage;
    }

    public void setRemoveTitleImage(Boolean removeTitleImage) {
        this.removeTitleImage = removeTitleImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitleImg() {
        return titleImg;
    }

    public void setTitleImg(String titleImg) {
        this.titleImg = titleImg;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public UserAccountDTO getOwner() {
        return owner;
    }

    public void setOwner(UserAccountDTO owner) {
        this.owner = owner;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public LocalDateTime getEditDateTime() {
        return editDateTime;
    }

    public void setEditDateTime(LocalDateTime editDateTime) {
        this.editDateTime = editDateTime;
    }
}
