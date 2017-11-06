package com.github.nkonev;

/**
 * Created by nik on 27.05.17.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nkonev.controllers.CommentControllerTest;
import com.github.nkonev.dto.PostDTO;
import com.github.nkonev.repo.redis.UserConfirmationTokenRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.AbstractConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.Ssl;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(
        classes = {Launcher.class, SwaggerConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@AutoConfigureMockMvc(printOnlyOnFailure = false, print = MockMvcPrint.LOG_DEBUG)
@AutoConfigureRestDocs(outputDir = TestConstants.RESTDOCS_SNIPPETS_DIR)
@Transactional
public abstract class AbstractUtTestRunner {

    @Autowired
    protected MockMvc mockMvc;

//    @Value("${server.port}")
//    protected int serverPort;
//
//    @Value("${server.contextPath}")
//    protected String contextPath;

    @Autowired
    protected UserConfirmationTokenRepository userConfirmationTokenRepository;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected AbstractConfigurableEmbeddedServletContainer abstractConfigurableEmbeddedServletContainer;

    public String urlWithContextPath(){
        Ssl ssl = abstractConfigurableEmbeddedServletContainer.getSsl();
        String protocol = ssl!=null && ssl.isEnabled() ? "https" : "http";
        return protocol+"://127.0.0.1:"+abstractConfigurableEmbeddedServletContainer.getPort()+abstractConfigurableEmbeddedServletContainer.getContextPath();
    }

    @Value(CommonTestConstants.USER)
    protected String username;

    @Value(CommonTestConstants.PASSWORD)
    protected String password;

    @Autowired
    protected ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractUtTestRunner.class);

    public PostDTO getPost(long postId) throws Exception {
        MvcResult getPostRequest = mockMvc.perform(
                get(Constants.Uls.API+Constants.Uls.POST+"/"+postId)
        )
                .andExpect(status().isOk())
                .andReturn();
        String getStr = getPostRequest.getResponse().getContentAsString();
        LOGGER.debug(getStr);
        return objectMapper.readValue(getStr, PostDTO.class);
    }

    @Before
    public void before() {
        userConfirmationTokenRepository.deleteAll();
    }
}
