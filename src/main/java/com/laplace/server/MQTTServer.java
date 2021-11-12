package com.laplace.server;


import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServer;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/12 13:32
 * @Info:
 * @Email:
 */
public class MQTTServer {
    public static void main(String[] args) {
        MqttServer mqttServer = MqttServer.create(Vertx.vertx());
        mqttServer.endpointHandler(endpoint -> {

            // 显示主要连接信息
            System.out.println("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());

            if (endpoint.auth() != null) {
                System.out.println("[username = " + endpoint.auth().userName() + ", password = " + endpoint.auth().password() + "]");
            }
            if (endpoint.will() != null) {
                System.out.println("[will topic = " + endpoint.will().willTopic() + " msg = " + endpoint.will().willMessage() +
                        " QoS = " + endpoint.will().willQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
            }

            System.out.println("[keep alive timeout = " + endpoint.keepAliveTimeSeconds() + "]");

            // 接受远程客户端连接
            endpoint.accept(false);

        })
                .listen(ar -> {

                    if (ar.succeeded()) {

                        System.out.println("MQTT server is listening on port " + ar.result().actualPort());
                    } else {

                        System.out.println("Error on starting the server");
                        ar.cause().printStackTrace();
                    }
                });
    }

}
