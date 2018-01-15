package com.github.nkonev.services;

import com.github.nkonev.entity.mongodb.ChatInfo;
import com.github.nkonev.repo.mongodb.ChatMetainfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Collections;


@Service
public class ChatService {

    @Autowired
    private ChatMetainfoRepository chatMetainfoRepository;

    public Flux<ChatInfo> getChats(long userId) {
        return chatMetainfoRepository.findAllByParticipantsIn(Collections.singletonList(userId));
    }
}
