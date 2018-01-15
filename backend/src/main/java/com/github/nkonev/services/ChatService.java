package com.github.nkonev.services;

import com.github.nkonev.dto.ChatInfoDto;
import com.github.nkonev.repo.mongodb.ChatMetainfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ChatService {

    @Autowired
    private ChatMetainfoRepository chatMetainfoRepository;

    public Collection<ChatInfoDto> getChats(long userId) {
        return chatMetainfoRepository.findAll();
    }
}
