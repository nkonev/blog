package com.github.nkonev.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;

// TODO to api
@Document(collection = "metainfo")
public class ChatInfoDto {
    @Id
    private String id;

    private String title;
    private Collection<Long> participants;

    public ChatInfoDto(String title, Collection<Long> participants) {
        this.title = title;
        this.participants = participants;
    }

    public ChatInfoDto() { }

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
