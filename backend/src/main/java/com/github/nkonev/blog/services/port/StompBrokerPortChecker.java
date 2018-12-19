package com.github.nkonev.blog.services.port;

import com.github.nkonev.blog.config.WebSocketConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service(StompBrokerPortChecker.NAME)
public class StompBrokerPortChecker extends AbstractPortChecker{

    public static final String NAME="stompBrokerPortChecker";

    @Value("${port.check.stomp.broker.max.count:64}")
    private int maxCount;

    @Autowired
    private WebSocketConfig.StompConfig stompConfig;

    private static final Logger LOGGER = LoggerFactory.getLogger(StompBrokerPortChecker.class);

    @PostConstruct
    public void checkPorts(){
        LOGGER.info("Will check stomp broker connection");
        check(maxCount, stompConfig.getHost(), stompConfig.getPort());
        LOGGER.info("Stomp broker connection is ok");
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
