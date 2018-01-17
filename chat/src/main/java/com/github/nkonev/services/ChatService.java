package com.github.nkonev.services;

import com.github.nkonev.entity.mongodb.ChatInfo;
import com.github.nkonev.repo.mongodb.ChatMetainfoRepository;
import com.github.nkonev.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.Collections;


@Service
public class ChatService {

    @Autowired
    private ChatMetainfoRepository chatMetainfoRepository;

    public Flux<ChatInfo> getChats(long userId, int page, int size) {
        PageRequest springDataPage = PageRequest.of(PageUtils.fixPage(page), PageUtils.fixSize(size));

        return chatMetainfoRepository.findAllByParticipantsIn(Collections.singletonList(userId), springDataPage);
    }
}
