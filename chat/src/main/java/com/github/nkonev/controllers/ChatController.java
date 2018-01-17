package com.github.nkonev.controllers;

import com.github.nkonev.Constants;
import com.github.nkonev.entity.mongodb.ChatInfo;
import com.github.nkonev.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {
    @Autowired
    private ChatService chatService;

    @GetMapping(Constants.Uls.API + Constants.Uls.CHATS)
    public Flux<ChatInfo> getChatInfos(
            long userId,
            @RequestParam(value = "page", required=false, defaultValue = "0") int page,
            @RequestParam(value = "size", required=false, defaultValue = "0") int size
    ) {
        return chatService.getChats(userId, page, size);
    }
}
