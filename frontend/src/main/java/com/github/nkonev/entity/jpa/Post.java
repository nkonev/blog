package com.github.nkonev.entity.jpa;

import com.github.nkonev.Constants;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    /**
     * text without html tags used for fulltext search and main page displaying
     */
    private String textNoTags;
    private String titleImg;

    @ManyToOne
    @JoinColumn(name="owner_id")
    private UserAccount owner;

    @Generated(GenerationTime.INSERT)
    private LocalDateTime createDateTime;

    public Post() { }

    public Post(Long id, String title, String text, String textNoTags, String titleImg) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.textNoTags = textNoTags;
        this.titleImg = titleImg;
    }

    public Post(Long id, String title, String text, String textNoTags, String titleImg, UserAccount owner) {
        this(id, title, text, textNoTags, titleImg);
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

    public String getTitleImg() {
        return titleImg;
    }

    public void setTitleImg(String titleImg) {
        this.titleImg = titleImg;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public void setOwner(UserAccount owner) {
        this.owner = owner;
    }

    public String getTextNoTags() {
        return textNoTags;
    }

    public void setTextNoTags(String textNoTags) {
        this.textNoTags = textNoTags;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }
}
