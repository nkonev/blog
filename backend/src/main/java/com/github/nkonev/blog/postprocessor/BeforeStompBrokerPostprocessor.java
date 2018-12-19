package com.github.nkonev.blog.postprocessor;

import com.github.nkonev.blog.config.WebSocketConfig;
import com.github.nkonev.blog.services.port.JdbcPortChecker;
import com.github.nkonev.blog.services.port.StompBrokerPortChecker;
import org.springframework.boot.autoconfigure.AbstractDependsOnBeanFactoryPostProcessor;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class BeforeStompBrokerPostprocessor extends AbstractDependsOnBeanFactoryPostProcessor {

	public BeforeStompBrokerPostprocessor() {
        super(WebSocketConfig.class, StompBrokerPortChecker.NAME);
    }
}
