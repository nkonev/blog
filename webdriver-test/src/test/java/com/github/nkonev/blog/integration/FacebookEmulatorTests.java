package com.github.nkonev.blog.integration;

import com.github.nkonev.blog.entity.jpa.UserAccount;
import com.github.nkonev.blog.repo.jpa.UserAccountRepository;
import com.github.nkonev.blog.webdriver.configuration.SeleniumConfiguration;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public abstract class FacebookEmulatorTests extends AbstractItTestRunner {
    private static final int MOCK_SERVER_PORT = 10080;

    protected static ClientAndServer mockServer;

    @Autowired
    protected UserAccountRepository userAccountRepository;

    @Autowired
    protected SeleniumConfiguration seleniumConfiguration;

    @BeforeClass
    public static void setUpClass() {
        mockServer = startClientAndServer(MOCK_SERVER_PORT);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        mockServer.stop();
    }

    public static final String facebookLogin = "Nikita K";

    @Before
    public void configureEmulator(){
        mockServer
                .when(request().withPath("/mock/facebook/dialog/oauth")).callback(httpRequest -> {
            String state = httpRequest.getQueryStringParameters().stream().filter(parameter -> "state".equals(parameter.getName().getValue())).findFirst().get().getValues().get(0).getValue();
            return response().withHeaders(
                    new Header(HttpHeaderNames.CONTENT_TYPE.toString(), "text/html; charset=\"utf-8\""),
                    new Header(HttpHeaderNames.LOCATION.toString(), urlPrefix+"/api/login/facebook?code=fake_code&state="+state)
            ).withStatusCode(302);
        });

        mockServer
                .when(request().withPath("/mock/facebook/oauth/access_token"))
                .respond(response().withHeaders(
                        new Header(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
                        ).withStatusCode(200).withBody("{\n" +
                                "  \"access_token\": \"fake-access-token\", \n" +
                                "  \"token_type\": \"bearer\",\n" +
                                "  \"expires_in\":  3600\n" +
                                "}")
                );

        mockServer
                .when(request().withPath("/mock/facebook/me"))
                .respond(response().withHeaders(
                        new Header(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
                        ).withStatusCode(200).withBody("{\n" +
                                "  \"id\": \"1234\", \n" +
                                "  \"name\": \""+facebookLogin+"\",\n" +
                                "  \"picture\": {\n" +
                                "      \"data\": {\t\n" +
                                "           \"url\": \"https://i.pinimg.com/236x/37/47/62/374762701f2571ffaacba61325d6dbf1--linux-penguin.jpg\"\n" +
                                "        }\n" +
                                "    }"+
                                "}")
                );

        userAccountRepository.findByUsername(facebookLogin).ifPresent(userAccount -> {
            userAccount.setLocked(false);
            userAccountRepository.save(userAccount);
        });
    }

    @After
    public void resetEmulator(){
        mockServer.reset();
    }
}
