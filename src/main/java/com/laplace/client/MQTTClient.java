package com.laplace.client;

import org.eclipse.paho.client.mqttv3.*;

import javax.security.auth.callback.Callback;


/**
 * @Author: YEP
 * @CreateDate: 2021/11/12 15:09
 * @Info:
 * @Email:
 */
public class MQTTClient {
    public static void main(String[] args) throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        MqttClient client = new MqttClient("tcp://10.10.10.15:1883", "userId");
        client.connect(options);
        client.subscribe("yepasdfs", 0, (s, mqttMessage) -> {
            System.out.println(s);
            System.out.println(mqttMessage.toString());
        });
        client.subscribe("my_topic", 0, (s, mqttMessage) -> {
            System.out.println(s);
            System.out.println(mqttMessage.toString());
        });
    }
}
