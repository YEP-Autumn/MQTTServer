package com.laplace.server.bean;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.mqtt.MqttEndpoint;
import lombok.Data;

import java.util.LinkedList;
import java.util.Objects;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/16 18:25
 * @Info:
 * @Email:
 */
@Data
public class MqttEndpointPower {

    private String clientIdentifier;


    private MqttQoS qoS;

    private boolean cleanSession;

    private boolean willFlag;

    private Topic will;

//    private LinkedList<Topic> offlineTopic;

    public MqttEndpointPower(MqttEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public MqttEndpointPower(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MqttEndpointPower that = (MqttEndpointPower) o;
        return Objects.equals(clientIdentifier, that.clientIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientIdentifier);
    }
}
