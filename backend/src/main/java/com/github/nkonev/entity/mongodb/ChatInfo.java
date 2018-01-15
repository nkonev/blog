package com.github.nkonev.entity.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;

import static com.github.nkonev.entity.mongodb.ChatInfo.COLLECTION_NAME;

@Document(collection = COLLECTION_NAME)
public class ChatInfo {
    public static final String COLLECTION_NAME = "metainfo";
    @Id
    private String id;

    private String title;
    private Collection<Long> participants;

    public ChatInfo(String title, Collection<Long> participants) {
        this.title = title;
        this.participants = participants;
    }

    public ChatInfo() { }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Collection<Long> getParticipants() {
        return participants;
    }

    public void setParticipants(Collection<Long> participants) {
        this.participants = participants;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
