package com.github.nkonev.blog.entity.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import static com.github.nkonev.blog.entity.elasticsearch.Post.INDEX;

@Document(indexName = INDEX, type = "_doc")
public class Post {

    public static final String INDEX = "post";

    public static final String FIELD_ID = "id";
    public static final String FIELD_TEXT = "text";
    public static final String FIELD_TITLE = "title";

    @Id
    private Long id;

    private String title;

    private String text;

    public Post() { }

    public Post(Long id, String title, String text) {
        this.id = id;
        this.title = title;
        this.text = text;
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
}
