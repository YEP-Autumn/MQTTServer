package com.laplace.client;

import com.sun.xml.internal.fastinfoset.util.StringArray;
import lombok.SneakyThrows;
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

    public static void main(String[] args) {
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                for (int i = 0; i < 500; i++) {
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setCleanSession(i / 2 == 0);
                    options.setKeepAliveInterval(60);
                    options.setUserName("2017248646");
                    options.setPassword("2017248646Ss.".toCharArray());
                    options.setAutomaticReconnect(true);
                    MqttClient client = new MqttClient("tcp://localhost:1883", "6fc1d4r15b54655fas8ab16ce6a76938d4" + i);  // test.ranye-iot.net
                    client.connect(options);
                    client.subscribe("www/a/" + (i + 5) + "skn/" + i * 2, 0, (s, mqttMessage) -> {
                        System.out.println(s);
                        System.out.println(mqttMessage.toString());
                    });
                    Thread.sleep((long) (Math.random() * 200 + 200));
                    client.subscribe("test" + i, 2, (s, mqttMessage) -> {
                        System.out.println(s);
                        System.out.println(mqttMessage.toString());
                    });
                    Thread.sleep((long) Math.random());
                }
            }
        }).start();


        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                for (int i = 0; i < 500; i++) {
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setCleanSession(i / 2 == 0);
                    options.setKeepAliveInterval(60);
                    options.setUserName("2017248646");
                    options.setPassword("2017248646Ss.".toCharArray());
                    options.setAutomaticReconnect(true);
                    MqttClient client = new MqttClient("tcp://localhost:1883", "6fc1d4r15b54655fas8ab16ce6a76938d4" + i);  // test.ranye-iot.net
                    client.connect(options);
                    client.subscribe("www/a/" + i + "/skn/" + i * 2, 1, (s, mqttMessage) -> {
                        System.out.println(s);
                        System.out.println(mqttMessage.toString());
                    });
                    Thread.sleep((long) (Math.random() * 200 + 200));
                    client.subscribe("test", 2, (s, mqttMessage) -> {
                        System.out.println(s);
                        System.out.println(mqttMessage.toString());
                    });
                    Thread.sleep((long) Math.random());
                }
            }
        }).start();


        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                for (int i = 0; i < 500; i++) {
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setCleanSession(i / 2 == 0);
                    options.setKeepAliveInterval(60);
                    options.setUserName("2017248646");
                    options.setPassword("2017248646Ss.".toCharArray());
                    options.setAutomaticReconnect(true);
                    MqttClient client = new MqttClient("tcp://localhost:1883", "6fc1d43rqb546558ab16ce6weq76938d4" + i);  // test.ranye-iot.net
                    client.connect(options);
                    client.subscribe("wwdw/a/" + (i + 1) + "/skan/" + i * 2, 2, (s, mqttMessage) -> {
                        System.out.println(s);
                        System.out.println(mqttMessage.toString());
                    });
                    Thread.sleep((long) (Math.random() * 200 + 200));
                    client.subscribe("test" + i, 2, (s, mqttMessage) -> {
                        System.out.println(s);
                        System.out.println(mqttMessage.toString());
                    });
                }
                Thread.sleep((long) Math.random());
            }
        }).start();

    }
}
