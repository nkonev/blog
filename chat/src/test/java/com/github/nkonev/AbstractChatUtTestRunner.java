package com.github.nkonev;

/**
 * Created by nik on 27.05.17.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(
        classes = {ChatApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
//@AutoConfigureMockMvc(printOnlyOnFailure = false, print = MockMvcPrint.LOG_DEBUG)
public abstract class AbstractChatUtTestRunner {

//    @Autowired
//    protected MockMvc mockMvc;

//    @Autowired
//    protected TestRestTemplate restTemplate;
//

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractChatUtTestRunner.class);

}
