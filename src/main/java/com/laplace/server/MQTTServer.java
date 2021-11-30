package com.laplace.server;


import com.laplace.server.manager.MQTTServices;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.messages.MqttSubscribeMessage;
import io.vertx.mqtt.messages.MqttUnsubscribeMessage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;
import java.security.Security;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/12 13:32
 * @Info:
 * @Email:
 */
@Component
public class MQTTServer {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Resource
    MQTTServices MQTTServices;

    public void start() {
        MqttServer mqttServer = MqttServer.create(Vertx.vertx());
        mqttServer.endpointHandler(endpoint -> {

            if (!MQTTServices.dealWithLogin(endpoint)) return;  // 如果验证不通过 不需要进行后续处理

            // 处理保持在线请求
            endpoint.pingHandler(new Handler<Void>() {
                @Override
                public void handle(Void unused) {
                    MQTTServices.pingManager(endpoint);
                }
            });

            // 处理订阅请求
            endpoint.subscribeHandler(new Handler<MqttSubscribeMessage>() {
                @Override
                public void handle(MqttSubscribeMessage mqttSubscribeMessage) {
                    MQTTServices.subscribeManager(mqttSubscribeMessage, endpoint);
                }
            });

            // 处理取消订阅请求
            endpoint.unsubscribeHandler(new Handler<MqttUnsubscribeMessage>() {
                @Override
                public void handle(MqttUnsubscribeMessage mqttUnsubscribeMessage) {
                    MQTTServices.unsubscribeManager(mqttUnsubscribeMessage, endpoint);
                }
            });

            // 处理客户端发布请求
            endpoint.publishHandler(new Handler<MqttPublishMessage>() {
                @Override
                public void handle(MqttPublishMessage mqttPublishMessage) {
                    MQTTServices.publishManager(mqttPublishMessage, endpoint);
                }
            });

            // 一、处理断开连接请求
            endpoint.disconnectHandler(new Handler<Void>() {
                @Override
                public void handle(Void unused) {
                    MQTTServices.disconnectManager(endpoint);

                }
            });

            // 二、处理close
            endpoint.closeHandler(new Handler<Void>() {
                @Override
                public void handle(Void unused) {
                    MQTTServices.close(endpoint);
                }
            });

            // 处理客户端异常断开
            endpoint.exceptionHandler(new Handler<Throwable>() {
                @Override
                public void handle(Throwable throwable) {
                    MQTTServices.exception(endpoint, throwable);
                }
            });

        }).listen(ar -> {
            MQTTServices.setListener(ar);
        });
    }

}
