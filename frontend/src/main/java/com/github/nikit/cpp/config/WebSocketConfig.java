package com.github.nikit.cpp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

// https://docs.spring.io/spring/docs/current/spring-framework-reference/html/websocket.html#websocket-server
// https://spring.io/guides/gs/messaging-stomp-websocket/
// https://stackoverflow.com/questions/33977844/spring-send-message-to-websocket-clients
// https://spring.io/blog/2015/10/13/react-js-and-spring-data-rest-part-4-events
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // the endpoint for websocket connections
        registry.addEndpoint("/stomp").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/");

        // use the /app prefix for others
        config.setApplicationDestinationPrefixes("/app");
    }

}