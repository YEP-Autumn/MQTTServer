package com.laplace.server;


import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.mqtt.MqttTopicSubscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.Charset;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

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

    public static void main(String[] args) {

//        MqttServerOptions options = new MqttServerOptions()
//                .setPort(8883)
//                .setKeyCertOptions(new PemKeyCertOptions()
//                        .setKeyPath("certificate/lzstarrynight.key")
//                        .setCertPath("certificate/lzstarrynight.pem"))
//                .setSsl(true);

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

            // 断开连接时
//            endpoint.disconnectHandler(new Handler<Void>() {
//                @Override
//                public void handle(Void unused) {
//                    System.out.print("用户");
//                    System.out.print(unused);
//                    System.out.println("下线了");
//                }
//            });


            // 处理确认订阅请求
            endpoint.subscribeHandler(subscribe ->{
                List<MqttQoS> grantedQosLevels =new ArrayList<>();
                for(MqttTopicSubscription s: subscribe.topicSubscriptions()){
                    System.out.println("Subscription for "+ s.topicName()+" with QoS "+ s.qualityOfService());
                    grantedQosLevels.add(s.qualityOfService());
                }
            // 确认订阅请求
                endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);
            });

            // 处理取消订阅请求
            endpoint.unsubscribeHandler(unsubscribe ->{
                for(String t: unsubscribe.topics()){
                    System.out.println("Unsubscription for "+ t);
                }
            //取消订阅请求
                endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
            });

            // 处理客户端发送的消息
            endpoint.publishHandler(message ->{
                System.out.println("Just received message ["+ message.payload().toString(Charset.defaultCharset())+"] with QoS ["+ message.qosLevel()+"]");
                if(message.qosLevel()==MqttQoS.AT_LEAST_ONCE){
                    endpoint.publishAcknowledge(message.messageId());
                }else if(message.qosLevel()==MqttQoS.EXACTLY_ONCE){
                    endpoint.publishRelease(message.messageId());
                }
            }).publishReleaseHandler(endpoint::publishComplete);



            // 主动发送消息到客户端
            // 例子, 发布一个QoS级别为2的消息
            endpoint.publish("my_topic",Buffer.buffer("Hello from the Vert.x MQTT server"),MqttQoS.AT_MOST_ONCE,false,false);
            // 选定handlers处理QoS 1与QoS 2
//            endpoint.publishAcknowledgeHandler(h->{
//                System.out.println("Received ack for message = ${messageId}");
//            }).publishReceivedHandler(h->{
//                endpoint.publishRelease(h);
//            }).publishCompleteHandler(h->{
//                System.out.println("Received ack for message = " + h);
//            });


        }).listen(ar -> {
            // 监听事件  ar -- new Handler<AsyncResult<MqttServer>>()
            if (ar.succeeded()) {

                System.out.println("MQTT server is listening on port " + ar.result().actualPort());
            } else {

                System.out.println("Error on starting the server");
                ar.cause().printStackTrace();
            }

        });
//        mqttServer.listen(ar -> {
//            if (ar.succeeded()) {
//
//                System.out.println("MQTT server is listening on port " + ar.result().actualPort());
//            } else {
//
//                System.out.println("Error on starting the server");
//                ar.cause().printStackTrace();
//            }
//        });


    }

}
