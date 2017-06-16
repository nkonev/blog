package com.github.nikit.cpp.integration;

/**
 * Created by nik on 27.05.17.
 */

import com.github.nikit.cpp.IntegrationTestConstants;
import com.github.nikit.cpp.Launcher;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(
        classes = {Launcher.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
public abstract class AbstractItTestRunner {

    @Value(IntegrationTestConstants.URL_PREFIX)
    protected String urlPrefix;

    @Value(IntegrationTestConstants.USER)
    protected String user;

    @Value(IntegrationTestConstants.PASSWORD)
    protected String password;

}
