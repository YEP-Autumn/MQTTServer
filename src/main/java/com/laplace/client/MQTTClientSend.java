package com.laplace.client;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;
import org.eclipse.paho.client.mqttv3.*;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/12 15:35
 * @Info:
 * @Email:
 */
public class MQTTClientSend {
    public static void main(String[] args) throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
//        options.setSSLProperties();
        options.setAutomaticReconnect(true);
        String bt = "你好啊";
        options.setWill("yepasdfs",bt.getBytes(StandardCharsets.UTF_8),0,false);
        MqttClient client = new MqttClient("tcp://10.10.10.15:1883","sendId");
        client.connect(options);
        MqttTopic yepasdfs = client.getTopic("yepasdfs");
        yepasdfs.publish(bt.getBytes(StandardCharsets.UTF_8), 0,false);
//        client.publish("my_topic", bt.getBytes(StandardCharsets.UTF_8), 0,false);


    }
}
