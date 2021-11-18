package com.laplace.client;

import com.sun.xml.internal.fastinfoset.util.StringArray;
import org.eclipse.paho.client.mqttv3.*;

import javax.security.auth.callback.Callback;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * @Author: YEP
 * @CreateDate: 2021/11/12 15:09
 * @Info:
 * @Email:
 */
public class MQTTClient {

    public static void main(String[] args) throws MqttException, InterruptedException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setKeepAliveInterval(60);
//        options.setUserName("2017248646");
        options.setPassword("2017248646Ss.".toCharArray());
//        options.setAutomaticReconnect(true);
        MqttClient client = new MqttClient("tcp://test.ranye-iot.net:1883", "6fc1d435d5b546558ab16ce6a76938d4");  // test.ranye-iot.net
        client.connect(options);
//        client.subscribe("w", 0, (s, mqttMessage) -> {
//            System.out.println(s);
//            System.out.println(mqttMessage.toString());
//        });
        Thread.sleep(10000);
        client.subscribe("test", 2, (s, mqttMessage) -> {
            System.out.println(s);
            System.out.println(mqttMessage.toString());
        });

//        Thread.sleep(15000);
//
//        client.disconnect();

    }
}
