package com.github.nikit.cpp.entity;

import com.github.nikit.cpp.Constants;

import javax.persistence.*;
import java.net.URL;

@Entity
@Table(name = "comment", schema = Constants.Schemas.POSTS)
public class Comment {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String text;

    @ManyToOne
    @JoinColumn(name="owner_id")
    private UserAccount owner;

    private long postId;

    public Comment() { }

    public Comment(Long id, String text, long postId) {
        this.id = id;
        this.text = text;
    }

    public Comment(Long id, String text, long postId, UserAccount owner) {
        this(id, text, postId);
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public void setOwner(UserAccount owner) {
        this.owner = owner;
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }
}
