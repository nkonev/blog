package com.github.nkonev.blog.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nkonev.blog.dto.PostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.github.nkonev.blog.converter.PostConverter.cleanHtmlTags;

@Service
public class WebSocketService {
    private final SimpMessagingTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public WebSocketService(SimpMessagingTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    private PostDTO deepCopy(PostDTO postDTO) throws IOException {
        String s = objectMapper.writeValueAsString(postDTO);
        return objectMapper.readValue(s, PostDTO.class);
    }

    public void sendInsertPostEvent(PostDTO postDTO) throws IOException {
        PostDTO copy = deepCopy(postDTO);
        copy.setText(cleanHtmlTags(postDTO.getText()));
        template.convertAndSend("/topic/posts.insert", copy);
    }

    public void sendUpdatePostEvent(PostDTO postDTO) throws IOException {
        PostDTO copy = deepCopy(postDTO);
        copy.setText(cleanHtmlTags(postDTO.getText()));
        template.convertAndSend("/topic/posts.update", copy);
    }

    public void sendDeletePostEvent(long id) {
        template.convertAndSend("/topic/posts.delete", id);
    }
}
