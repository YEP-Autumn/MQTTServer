package com.laplace.server.bean;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;
import lombok.Data;

import java.util.Objects;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/15 13:00
 * @Info:
 * @Email:
 */
@Data
public class Topic {

    // 主要标识信息
    private String topicName;
    private MqttQoS qos;

    // 附带信息
    private Buffer payload;
    private boolean Dup;
    private boolean Retain;


    public Topic(String topicName) {
        this.topicName = topicName;
    }

    public Topic(String topicName, MqttQoS qos, Buffer payload, boolean dup, boolean retain) {
        this.topicName = topicName;
        this.qos = qos;
        this.payload = payload;
        this.Dup = dup;
        this.Retain = retain;
    }

    public Topic(String topicName, MqttQoS qos) {
        this.topicName = topicName;
        this.qos = qos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return Objects.equals(topicName, topic.topicName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName);
    }
}
