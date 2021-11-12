package com.laplace.client;

import io.netty.handler.codec.mqtt.MqttProperties;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.support.MqttUtils;

import javax.xml.bind.Marshaller;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/12 15:09
 * @Info:
 * @Email:
 */
public class MQTTClient {
    public static void main(String[] args) throws URISyntaxException, MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        MqttClient client = new MqttClient("tcp://10.10.10.15:1883","userId");
        client.connect(options);
        client.subscribe("temp", new IMqttMessageListener() {
            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                System.out.println(s);
                System.out.println(mqttMessage.toString());
            }
        });
    }
}
