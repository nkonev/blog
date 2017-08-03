package com.github.nikit.cpp.dto;

public class CommentDTO {
    private long id;
    private String text;

    public CommentDTO() { }

    public CommentDTO(long id, String text) {
        this.id = id;
        this.text = text;
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
}
