package com.github.nikit.cpp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
public class WebSocketController {
    private SimpMessagingTemplate template;

    @Autowired
    public WebSocketController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @GetMapping(path="/api/public/greetings")
    @ResponseBody
    public void greet() {
        PostController.PostDTO postDTO = new PostController.PostDTO(2_000_000, "Post via websocket", "Пост, пришедший через вебсокет " + new Date(), null);
        this.template.convertAndSend("/topic/posts", postDTO);
    }
}
