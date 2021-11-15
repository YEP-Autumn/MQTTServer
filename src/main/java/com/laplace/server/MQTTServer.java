package com.laplace.server;


import com.laplace.server.bean.Topic;
import com.laplace.server.bean.TopicManager;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.messages.MqttSubscribeMessage;
import io.vertx.mqtt.messages.MqttUnsubscribeMessage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


import java.security.Security;
import java.util.ArrayList;
import java.util.HashSet;
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

    //    @Resource
    TopicManager topicManager = new TopicManager();

    private HashSet<String> clientID = new HashSet<>();

    public void start() {
        MqttServer mqttServer = MqttServer.create(Vertx.vertx());
        mqttServer.endpointHandler(endpoint -> {

            // 接受客户端连接
            System.out.println("有新客户端连接");

            // 如果集合中不存在这个ID 说明客户端未曾连接
            if (!clientID.contains(endpoint.clientIdentifier())) {

                // 打印用户登录信息
                System.out.println("【ClientID】:" + endpoint.clientIdentifier() + " 【连接状态】" + endpoint.isConnected() + " 【CleanSession】" + endpoint.isCleanSession());

                // 处理客户端的用户名密码
                if (endpoint.auth() == null) {
                    // 如果没有密码拒绝连接
                    endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED);
                    return;
                }

                // 打印用户名密码信息
                System.out.println("[username = " + endpoint.auth().userName() + ", password = " + endpoint.auth().password() + "]");
                if (!("2017248646".equals(endpoint.auth().userName()) || "2017248646Ss.".equals(endpoint.auth().password()))) {
                    // 如果密码错误拒绝连接
                    endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
                    return;
                }

                // 处理客户端的遗嘱
                if (endpoint.will() != null) {
                    System.out.println("遗嘱不为空   【主题】:" + endpoint.will().willTopic() + " 【服务质量】:" + endpoint.will().willQos() + " 【Message】:" + endpoint.will().willMessage() + " 【保留】:" + endpoint.will().isWillRetain() + " 【Flag】:" + endpoint.will().isWillFlag());
                }

                // 接收用户连接请求
                clientID.add(endpoint.clientIdentifier());
                endpoint.accept(false);  // 是否存在上一个会话
            } else {
                // 如果客户端ClientID已被使用
                endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED);
                return;
            }


            // 处理订阅请求
            endpoint.subscribeHandler(new Handler<MqttSubscribeMessage>() {
                @Override
                public void handle(MqttSubscribeMessage mqttSubscribeMessage) {
                    List<MqttQoS> grantedQosLevels = new ArrayList<>();
                    for (MqttTopicSubscription s : mqttSubscribeMessage.topicSubscriptions()) {
                        System.out.println("【订阅主题】:" + s.topicName() + " 【服务质量】:" + s.qualityOfService());
                        grantedQosLevels.add(s.qualityOfService());

                        topicManager.subscribe(new Topic(s.topicName(), s.qualityOfService()), endpoint);  // 处理主题订阅
                    }
                    endpoint.subscribeAcknowledge(mqttSubscribeMessage.messageId(), new ArrayList<>());
                }
            });

            // 处理取消订阅请求
            endpoint.unsubscribeHandler(new Handler<MqttUnsubscribeMessage>() {
                @Override
                public void handle(MqttUnsubscribeMessage mqttUnsubscribeMessage) {
                    System.out.println("客户端取消订阅  【主题】:" + mqttUnsubscribeMessage.topics().toString() + " 【MessageID】:" + mqttUnsubscribeMessage.messageId());
                    for (String topic : mqttUnsubscribeMessage.topics()) {
                        topicManager.unSubscribe(new Topic(topic), endpoint);
                    }

                    endpoint.unsubscribeAcknowledge(mqttUnsubscribeMessage.messageId());
                }
            });

            // 处理客户端发布请求
            endpoint.publishHandler(new Handler<MqttPublishMessage>() {
                @Override
                public void handle(MqttPublishMessage mqttPublishMessage) {
                    // endpoint.publishAutoAck(true);  // 自动处理响应
                    System.out.println("客户端发布主题   【主题】:" + mqttPublishMessage.topicName() +
                            " 【服务质量】:" + mqttPublishMessage.qosLevel() +
                            " 【Message】:" + mqttPublishMessage.payload().toString() +
                            " 【保留】:" + mqttPublishMessage.isRetain() +
                            " 【重复】:" + mqttPublishMessage.isDup());  // 如果是重复消息说明该设备网络不好，曾经发送过数据但没有收到返回消息(服务质量不为0，cleanSession=false)


                    // QoS = 0
                    if (mqttPublishMessage.qosLevel() == MqttQoS.AT_MOST_ONCE) {
                        Topic topic = new Topic(mqttPublishMessage.topicName(), mqttPublishMessage.qosLevel(), mqttPublishMessage.payload(), mqttPublishMessage.isDup(), mqttPublishMessage.isRetain());
                        topicManager.sendTopic(topic);
                        return;
                    }

                    /*
                        QoS = 1
                                    (PUBLISH)
                        publisher   ---------->  Broker
                                    (PUBACK)
                        publisher   <----------  Broker
                    */
                    if (mqttPublishMessage.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                        endpoint.publishAcknowledge(mqttPublishMessage.messageId());
                        Topic topic = new Topic(mqttPublishMessage.topicName(), mqttPublishMessage.qosLevel(), mqttPublishMessage.payload(), mqttPublishMessage.isDup(), mqttPublishMessage.isRetain());
                        topicManager.sendTopic(topic);
                        return;
                    }


                    /*
                        QoS = 2
                                    (PUBLISH)
                        publisher   ---------->  Broker
                                    (PUBREC)
                        publisher   <----------  Broker
                                    (PUBREL)
                        publisher   ---------->  Broker
                                    (PUBCOMP)
                        publisher   <----------  Broker
                    */
                    if (mqttPublishMessage.qosLevel() == MqttQoS.EXACTLY_ONCE) {
                        endpoint.publishReceived(mqttPublishMessage.messageId()).publishReleaseHandler(new Handler<Integer>() {
                            @Override
                            public void handle(Integer integer) {
                                endpoint.publishComplete(integer);
                                Topic topic = new Topic(mqttPublishMessage.topicName(), mqttPublishMessage.qosLevel(), mqttPublishMessage.payload(), mqttPublishMessage.isDup(), mqttPublishMessage.isRetain());
                                topicManager.sendTopic(topic);
                                return;
                            }
                        });
                    }


                }
            });

            // 一、处理断开连接请求
            endpoint.disconnectHandler(new Handler<Void>() {
                @Override
                public void handle(Void unused) {
                    clientID.remove(endpoint.clientIdentifier());
                    System.out.println("用户【" + endpoint.clientIdentifier() + "】断开连接");
                    System.out.println(endpoint.isConnected());  // true
                }
            });
            // 二、处理close
            endpoint.closeHandler(new Handler<Void>() {
                @Override
                public void handle(Void unused) {
                    System.out.println("用户【" + endpoint.clientIdentifier() + "】Close");
                    System.out.println(endpoint.isConnected());  // false
                }
            });
        }).listen(ar -> {
            // 监听事件  ar -- new Handler<AsyncResult<MqttServer>>()
            if (ar.succeeded()) {
                System.out.println("MQTT server is listening on port " + ar.result().actualPort());
            } else {

                System.out.println("Error on starting the server");
                ar.cause().printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        MQTTServer server = new MQTTServer();
        server.start();
    }
}
