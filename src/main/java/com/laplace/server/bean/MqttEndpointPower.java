package com.laplace.server.bean;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.mqtt.MqttEndpoint;
import lombok.Data;

import java.util.Objects;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/16 18:25
 * @Info:
 * @Email:
 */
@Data
public class MqttEndpointPower {

    private MqttEndpoint endpoint;

    private MqttQoS qoS;

    private boolean willFlag;

    private Topic will;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MqttEndpointPower that = (MqttEndpointPower) o;
        return Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint);
    }

    @Override
    public String toString() {
        return
                "qoS=" + qoS
             ;
    }
}
