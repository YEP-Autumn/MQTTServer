package com.laplace.server;


import com.laplace.server.manager.EndpointManager;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.messages.MqttSubscribeMessage;
import io.vertx.mqtt.messages.MqttUnsubscribeMessage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


import java.security.Security;
import java.sql.Timestamp;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/12 13:32
 * @Info:
 * @Email:
 */
public class MQTTServer {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    //    @Resource
    EndpointManager endpointManager = new EndpointManager();


    public void start() {
        MqttServer mqttServer = MqttServer.create(Vertx.vertx());
        mqttServer.endpointHandler(endpoint -> {
            endpointManager.dealWithLogin(endpoint);

            // 处理保持在线请求
            endpoint.pingHandler(new Handler<Void>() {
                @Override
                public void handle(Void unused) {
                    endpointManager.pingManager(endpoint);
                }
            });

            // 处理订阅请求
            endpoint.subscribeHandler(new Handler<MqttSubscribeMessage>() {
                @Override
                public void handle(MqttSubscribeMessage mqttSubscribeMessage) {
                    endpointManager.subscribeManager(mqttSubscribeMessage, endpoint);
                }
            });

            // 处理取消订阅请求
            endpoint.unsubscribeHandler(new Handler<MqttUnsubscribeMessage>() {
                @Override
                public void handle(MqttUnsubscribeMessage mqttUnsubscribeMessage) {
                    endpointManager.unsubscribeManager(mqttUnsubscribeMessage, endpoint);
                }
            });

            // 处理客户端发布请求
            endpoint.publishHandler(new Handler<MqttPublishMessage>() {
                @Override
                public void handle(MqttPublishMessage mqttPublishMessage) {
                    endpointManager.publishManager(mqttPublishMessage, endpoint);

                }
            });

            // 一、处理断开连接请求
            endpoint.disconnectHandler(new Handler<Void>() {
                @Override
                public void handle(Void unused) {
                    endpointManager.disconnectManager(endpoint);

                }
            });

            // 二、处理close
            endpoint.closeHandler(new Handler<Void>() {
                @Override
                public void handle(Void unused) {
                    endpointManager.close(endpoint);
                }
            });

            // 处理客户端异常断开
            endpoint.exceptionHandler(new Handler<Throwable>() {
                @Override
                public void handle(Throwable throwable) {
                    endpointManager.exception(endpoint,throwable);
                }
            });

        }).listen(ar -> {
            endpointManager.setListener(ar);
        });
    }

    public static void main(String[] args) {
        MQTTServer server = new MQTTServer();
        server.start();
    }
}
