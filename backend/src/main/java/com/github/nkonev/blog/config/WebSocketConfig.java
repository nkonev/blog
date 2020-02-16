package com.github.nkonev.blog.config;

import io.lettuce.core.ClientOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpLogging;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompDecoder;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.tcp.reactor.ReactorNettyCodec;
import org.springframework.messaging.tcp.reactor.ReactorNettyTcpClient;
import org.springframework.session.Session;
import org.springframework.session.web.socket.config.annotation.AbstractSessionWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import reactor.netty.tcp.TcpClient;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// https://docs.spring.io/spring/docs/current/spring-framework-reference/html/websocket.html#websocket-server
// https://spring.io/guides/gs/messaging-stomp-websocket/
// https://stackoverflow.com/questions/33977844/spring-send-message-to-websocket-clients
// https://spring.io/blog/2015/10/13/react-js-and-spring-data-rest-part-4-events
// https://docs.spring.io/spring-session/docs/1.3.1.RELEASE/reference/html5/guides/websocket.html
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractSessionWebSocketMessageBrokerConfigurer<Session> {

    @Override
    public void configureStompEndpoints(StompEndpointRegistry registry) {
        // the endpoint for websocket connections
        registry.addEndpoint("/stomp").setAllowedOrigins("*");
    }

    @Configuration
    @ConfigurationProperties(prefix = "custom.stomp.broker")
    public static class StompConfig {
        private String virtualHost;
        private String clientLogin;
        private String clientPassword;
        private String systemLogin;
        private String systemPassword;
        private String host;
        private int port;

        public StompConfig() { }

        public String getVirtualHost() {
            return virtualHost;
        }

        public void setVirtualHost(String virtualHost) {
            this.virtualHost = virtualHost;
        }

        public String getClientLogin() {
            return clientLogin;
        }

        public void setClientLogin(String clientLogin) {
            this.clientLogin = clientLogin;
        }

        public String getClientPassword() {
            return clientPassword;
        }

        public void setClientPassword(String clientPassword) {
            this.clientPassword = clientPassword;
        }

        public String getSystemLogin() {
            return systemLogin;
        }

        public void setSystemLogin(String systemLogin) {
            this.systemLogin = systemLogin;
        }

        public String getSystemPassword() {
            return systemPassword;
        }

        public void setSystemPassword(String systemPassword) {
            this.systemPassword = systemPassword;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    @Autowired
    private StompConfig stompConfig;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config
                // allowed prefixes here http://www.rabbitmq.com/stomp.html#d
                .enableStompBrokerRelay("/queue/", "/topic/")
                .setVirtualHost(stompConfig.getVirtualHost()) // vhost in RabbitMQ
                .setClientLogin(stompConfig.getClientLogin())
                .setClientPasscode(stompConfig.getClientPassword())
                .setSystemLogin(stompConfig.getSystemLogin())
                .setSystemPasscode(stompConfig.getSystemPassword())
//                .setRelayHost(stompConfig.getHost())
//                .setRelayPort(stompConfig.getPort())
                .setTcpClient(createTcpClient());
        ;

        // use the /app prefix for others
        config.setApplicationDestinationPrefixes("/app");
    }

    /*private ReactorNettyTcpClient<byte[]> createTcpClient() {
        StompDecoder decoder = new StompDecoder();
        ReactorNettyCodec<byte[]> codec = new StompReactorNettyCodec(decoder);
        ReactorNettyTcpClient<byte[]> client = new ReactorNettyTcpClient<>(stompConfig.getHost(), stompConfig.getPort(), codec);
        client.setLogger(SimpLogging.forLog(client.getLogger()));
        return client;
    }*/


    /*private ReactorNettyTcpClient<byte[]> createTcpClient() {
//        final List<InetSocketAddress> addressList = new ArrayList<>();
//        addressList.add(new InetSocketAddress("192.168.0.1", 61613));
//        addressList.add(new InetSocketAddress("192.168.0.2", 61613));
//        addressList.add(new InetSocketAddress("192.168.0.3", 61613));
//        addressList.add(new InetSocketAddress("192.168.0.4", 61613));
//        final RoundRobinList<InetSocketAddress> addresses = new RoundRobinList<>(addressList);
//        final Consumer<ClientOptions.Builder<?>> builderConsumer = b -> {
//            b.connectAddress(() -> { return addresses.getNext(); });
//        };
        return new ReactorNettyTcpClient<byte[]>(client -> client.addressSupplier(getAddressSupplier()), new StompReactorNettyCodec());
    }*/

    private Supplier<SocketAddress> getAddressSupplier() {
        return () -> {
            return new InetSocketAddress("localhost", 1234);
        };
    }


    private ReactorNettyTcpClient<byte[]> createTcpClient() {
//        final List<InetSocketAddress> addressList = new ArrayList<>();
//        addressList.add(new InetSocketAddress("192.168.0.1", 61613));
//        addressList.add(new InetSocketAddress("192.168.0.2", 61613));
//        addressList.add(new InetSocketAddress("192.168.0.3", 61613));
//        addressList.add(new InetSocketAddress("192.168.0.4", 61613));
//        final RoundRobinList<InetSocketAddress> addresses = new RoundRobinList<>(addressList);
//        final Consumer<ClientOptions.Builder<?>> builderConsumer = b -> {
//            b.connectAddress(() -> { return addresses.getNext(); });
//        };
        TcpClient tcpClient = TcpClient.create();
        return new ReactorNettyTcpClient<byte[]>(tcpClient, new StompReactorNettyCodec());
    }
}