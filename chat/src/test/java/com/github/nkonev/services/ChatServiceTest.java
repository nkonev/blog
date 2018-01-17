package com.github.nkonev.services;

import com.github.nkonev.AbstractChatUtTestRunner;
import com.github.nkonev.entity.mongodb.ChatInfo;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import java.util.Collection;
import static com.github.nkonev.CommonTestConstants.CHAT_USER_ID;
import static com.github.nkonev.CommonTestConstants.USER_1_CHATS;

public class ChatServiceTest extends AbstractChatUtTestRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServiceTest.class);

    @Autowired
    private ChatService service;

    @Test
    public void getChatInfos() throws Exception {
        Collection<ChatInfo> ch = service.getChats(CHAT_USER_ID, 0, 1000).collectList().block();
        Assert.assertEquals(20, ch.size());
    }


    @Test
    public void getChatInfos2() throws Exception {
        StepVerifier
                .create(service.getChats(CHAT_USER_ID, 0, 1000))
                .expectSubscription()
                .expectNextCount(20);
    }

}
