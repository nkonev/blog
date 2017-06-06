package com.github.nikit.cpp.configuration;

import com.github.nikit.cpp.IntegrationTestConstants;
import com.github.nikit.cpp.selenium.Browser;
import com.github.nikit.cpp.selenium.SeleniumFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Created by nik on 04.10.16.
 */
@Configuration
public class SeleniumConfiguration {

    @Value(IntegrationTestConstants.IMPLICITLY_WAIT_TIMEOUT)
    private int implicitlyWaitTimeout;

    @Value(IntegrationTestConstants.BROWSER)
    private Browser browser;

    /**
     * @Scope("singleton") is need as part of https://github.com/spring-projects/spring-boot/issues/7454
     * @return
     * @throws Exception
     */
    @Scope("singleton")
    @Bean(initMethod = "start", destroyMethod = "stop")
    public SeleniumFactory seleniumComponent() throws Exception {
        return new SeleniumFactory(implicitlyWaitTimeout, browser);
    }
}
