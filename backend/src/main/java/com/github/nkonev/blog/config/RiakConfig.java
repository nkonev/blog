package com.github.nkonev.blog.config;

import com.basho.riak.client.api.RiakClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.UnknownHostException;

@Configuration
public class RiakConfig {

    @Value("${custom.riak.host}")
    private String host;

    @Value("${custom.riak.port}")
    private int port;

    @Bean(destroyMethod = "shutdown")
    public RiakClient riakClient() throws UnknownHostException {
        RiakClient client = RiakClient.newClient(port, host);
        return client;
    }
}
