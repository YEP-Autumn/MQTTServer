package com.laplace.server.bean;

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

    private String clientIdentifier;

    private boolean isActiveDisconnect; // 是否是主动断开连接

    public MqttEndpointPower(MqttEndpoint endpoint) {
        this.endpoint = endpoint;
        this.clientIdentifier = endpoint.clientIdentifier();
        this.isActiveDisconnect = false;
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
