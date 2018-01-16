package com.github.nkonev.controllers;

import com.github.nkonev.AbstractChatUtTestRunner;
import com.github.nkonev.Constants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import static com.github.nkonev.CommonTestConstants.CHAT_USER_ID;
import static com.github.nkonev.CommonTestConstants.USER_1_CHATS;

public class ChatControllerTest extends AbstractChatUtTestRunner {

    @Test
    public void testGetChats() throws Exception {
       webTestClient
               .get()
               .uri(Constants.Uls.API + Constants.Uls.CHATS+ "?userId="+CHAT_USER_ID)
               .accept(MediaType.APPLICATION_JSON_UTF8)
               .exchange()
               .expectStatus().isOk()
               .expectBody().jsonPath("$.size()").isEqualTo(USER_1_CHATS);
    }
}
