package com.github.nikit.cpp.services;

import com.github.nikit.cpp.dto.PostDTO;
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

    public void sendPostDto(PostDTO postDTO) {
        this.template.convertAndSend("/topic/posts", postDTO);
    }
}
