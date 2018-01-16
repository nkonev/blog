package com.github.nkonev.controllers;

import com.github.nkonev.Constants;
import com.github.nkonev.entity.mongodb.ChatInfo;
import com.github.nkonev.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {
    @Autowired
    private ChatService chatService;

    @GetMapping(Constants.Uls.API + Constants.Uls.CHATS)
    public Flux<ChatInfo> getChatInfos(long userId) {
        return chatService.getChats(userId);
    }
}
