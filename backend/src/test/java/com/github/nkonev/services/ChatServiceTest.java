package com.github.nkonev.services;

import com.github.nkonev.AbstractUtTestRunner;
import com.github.nkonev.CommonTestConstants;
import com.github.nkonev.dto.ChatInfoDto;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;

public class ChatServiceTest extends AbstractUtTestRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServiceTest.class);

    @Autowired
    private ChatService service;

    @Value(CommonTestConstants.USER_ID)
    private long userId;

    @Test
    public void getChatInfos() throws Exception {
        Collection<ChatInfoDto> ch = service.getChats(userId);
        Assert.assertEquals(ch.size(), 1000);
    }

}
