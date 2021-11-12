package com.laplace.client;

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
    public static void main(String[] args) throws URISyntaxException, MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        String bt = "你好啊";
        options.setWill("temp",bt.getBytes(StandardCharsets.UTF_8),1,false);
        MqttClient client = new MqttClient("tcp://10.10.10.15:1883","sendId");
        client.connect(options);
    }
}
