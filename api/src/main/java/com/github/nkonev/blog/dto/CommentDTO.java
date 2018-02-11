package com.github.nkonev.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.nkonev.blog.ApiConstants;

import java.time.LocalDateTime;

public class CommentDTO {
    private long id;
    private String text;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= ApiConstants.DATE_FORMAT)
    private LocalDateTime createDateTime;

    public CommentDTO() { }

    public CommentDTO(long id, String text, LocalDateTime createDateTime) {
        this.id = id;
        this.text = text;
        this.createDateTime = createDateTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }
}
