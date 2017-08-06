package com.github.nikit.cpp.entity.jpa;

import com.github.nikit.cpp.Constants;

import javax.persistence.*;
import java.net.URL;

/**
 * This entity "Post" don't have comments because there isn't always need to get Post with Collection<Comment>
 */
@Entity
@Table(name = "post", schema = Constants.Schemas.POSTS)
public class Post {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String title;
    private String text;
    private URL titleImg;

    @ManyToOne
    @JoinColumn(name="owner_id")
    private UserAccount owner;

    public Post() { }

    public Post(Long id, String title, String text, URL titleImg) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.titleImg = titleImg;
    }

    public Post(Long id, String title, String text, URL titleImg, UserAccount owner) {
        this(id, title, text, titleImg);
        this.owner = owner;
    }

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

    public UserAccount getOwner() {
        return owner;
    }

    public void setOwner(UserAccount owner) {
        this.owner = owner;
    }
}
