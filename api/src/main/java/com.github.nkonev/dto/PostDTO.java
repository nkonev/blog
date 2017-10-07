package com.github.nkonev.dto;

public class PostDTO {
    private long id;
    private String title;
    private String text;
    private String titleImg;

    public PostDTO() { }

    public PostDTO(long id, String title, String text, String titleImg) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.titleImg = titleImg;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

}
