package com.laplace.client;

import com.sun.xml.internal.fastinfoset.util.StringArray;
import org.eclipse.paho.client.mqttv3.*;

import javax.security.auth.callback.Callback;
import java.util.ArrayList;
import java.util.List;


/**
 * @Author: YEP
 * @CreateDate: 2021/11/12 15:09
 * @Info:
 * @Email:
 */
public class MQTTClient {

    public static void main(String[] args) throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("2017248646");
        options.setPassword("2017248646Ss.".toCharArray());
        options.setAutomaticReconnect(true);
        MqttClient client = new MqttClient("tcp://localhost:1883", "6fc1d435d5b546558ab16ce6a76938d4");
        client.connect(options);
        client.subscribe("yepasdfs", 0, (s, mqttMessage) -> {
            System.out.println(s);
            System.out.println(mqttMessage.toString());
        });
        client.subscribe("my_topic", 2, (s, mqttMessage) -> {
            System.out.println(s);
            System.out.println(mqttMessage.toString());
        });
//        client.subscribeWithResponse();

    }
}
