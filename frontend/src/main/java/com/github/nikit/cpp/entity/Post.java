package com.github.nikit.cpp.entity;

import javax.persistence.*;
import java.net.URL;

@Entity
@Table(name = "post", schema = "posts")
public class Post {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String title;
    private String text;
    private URL titleImg;

    private long ownerId;

    public Post() { }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }
}
