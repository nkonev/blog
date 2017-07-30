package com.github.nikit.cpp.dto;

import java.net.URL;

public class PostDTO {
    private long id;
    private String title;
    private String text;
    private URL titleImg;

    public PostDTO() { }

    public PostDTO(long id, String title, String text, URL titleImg) {
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

    public URL getTitleImg() {
        return titleImg;
    }

    public void setTitleImg(URL titleImg) {
        this.titleImg = titleImg;
    }

}
