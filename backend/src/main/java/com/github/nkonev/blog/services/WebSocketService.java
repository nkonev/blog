package com.github.nkonev.blog.services;

import com.github.nkonev.blog.dto.PostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {
    private final SimpMessagingTemplate template;

    @Autowired
    public WebSocketService(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void sendInsertPostEvent(PostDTO postDTO) {
        template.convertAndSend("/topic/posts.insert", postDTO);
    }

    public void sendUpdatePostEvent(PostDTO postDTO) {
        template.convertAndSend("/topic/posts.update", postDTO);
    }

    public void sendDeletePostEvent(long id) {
        template.convertAndSend("/topic/posts.delete", id);
    }
}
