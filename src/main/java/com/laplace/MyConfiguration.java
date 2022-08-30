package com.laplace;


import com.laplace.server.MQTTServer;
import com.laplace.server.core.RankTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Configuration
public class MyConfiguration {

    @Bean
    public RankTopic getInetSocketAddress() {
        return new RankTopic("$");
    }

    @Bean
    public MQTTServer getMQTTServer() {
        MQTTServer server = new MQTTServer();
        server.start();
        return server;
    }

}
