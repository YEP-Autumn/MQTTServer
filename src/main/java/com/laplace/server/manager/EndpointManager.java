package com.laplace.server.manager;

import com.laplace.server.bean.Topic;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.messages.MqttSubscribeMessage;
import io.vertx.mqtt.messages.MqttUnsubscribeMessage;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/16 14:52
 * @Info:
 * @Email:
 */
public class EndpointManager {

    //    @Resource
    TopicManager topicManager = new TopicManager();

    private HashSet<String> clientID = new HashSet<>();


    public void dealWithLogin(MqttEndpoint endpoint) {
        // 接受客户端连接
        System.out.println("有新客户端连接");
        // 如果集合中不存在这个ID 说明客户端未曾连接
        if (!clientID.contains(endpoint.clientIdentifier())) {

            // 打印用户登录信息
            System.out.println("【ClientID】:" + endpoint.clientIdentifier() + " 【连接状态】" + endpoint.isConnected() + " 【CleanSession】" + endpoint.isCleanSession());

            // 处理客户端的用户名密码
            if (endpoint.auth() == null) {
                // 如果没有密码拒绝连接
                System.out.println("用户连接没有携带用户名密码");
                endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED);
                return;
            }

            // 打印用户名密码信息
            System.out.println("[username = " + endpoint.auth().userName() + ", password = " + endpoint.auth().password() + "]");
            if (!("2017248646".equals(endpoint.auth().userName()) || "2017248646Ss.".equals(endpoint.auth().password()))) {
                // 如果密码错误拒绝连接
                System.out.println("不支持的用户名密码");
                endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
                return;
            }

            // 处理客户端的遗嘱
//            if (endpoint.will().isWillFlag() && !StringUtil.isNullOrEmpty(endpoint.will().willTopic()) && !StringUtil.isNullOrEmpty(endpoint.will().willMessage())) {
            System.out.println("遗嘱:" + endpoint.will().isWillFlag() + " 【主题】:" + endpoint.will().willTopic() + " 【服务质量】:" + endpoint.will().willQos() + " 【Message】:" + endpoint.will().willMessage() + " 【保留】:" + endpoint.will().isWillRetain() + " 【Flag】:" + endpoint.will().isWillFlag());
//            }
            // 接收用户连接请求
            clientID.add(endpoint.clientIdentifier());
            endpoint.autoKeepAlive(false);  // 自动保持连接,默认为true
            endpoint.accept(false);  // 是否存在未确认的会话 sessionPresent

        } else {
            // 如果客户端ClientID已被使用
            System.out.println("客户端ID:" + endpoint.clientIdentifier() + "已被使用");
            endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED);
            return;
        }
    }

    public void publishManager(MqttPublishMessage mqttPublishMessage, MqttEndpoint endpoint) {
        // endpoint.publishAutoAck(true);  // 自动处理响应
        System.out.println("客户端发布主题   【主题】:" + mqttPublishMessage.topicName() +
                " 【服务质量】:" + mqttPublishMessage.qosLevel() +
                " 【Message】:" + mqttPublishMessage.payload().toString() +
                " 【保留】:" + mqttPublishMessage.isRetain() +
                " 【重复】:" + mqttPublishMessage.isDup());  // 如果是重复消息说明该设备网络不好，曾经发送过数据但没有收到返回消息(服务质量不为0，cleanSession=false)


        // QoS = 0
        if (mqttPublishMessage.qosLevel() == MqttQoS.AT_MOST_ONCE) {
            Topic topic = new Topic(mqttPublishMessage.topicName(), mqttPublishMessage.qosLevel(), mqttPublishMessage.payload(), false, false);
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
            Topic topic = new Topic(mqttPublishMessage.topicName(), mqttPublishMessage.qosLevel(), mqttPublishMessage.payload(), false, false);
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
                    Topic topic = new Topic(mqttPublishMessage.topicName(), mqttPublishMessage.qosLevel(), mqttPublishMessage.payload(), false, false);
                    topicManager.sendTopic(topic);
                    return;
                }
            });
        }
    }

    public void subscribeManager(MqttSubscribeMessage mqttSubscribeMessage, MqttEndpoint endpoint) {
        List<MqttQoS> grantedQosLevels = new ArrayList<>();
        for (MqttTopicSubscription s : mqttSubscribeMessage.topicSubscriptions()) {
            System.out.println("【订阅主题】:" + s.topicName() + " 【服务质量】:" + s.qualityOfService());
            grantedQosLevels.add(s.qualityOfService());

            topicManager.subscribe(new Topic(s.topicName(), s.qualityOfService()), endpoint);  // 处理主题订阅
        }
        endpoint.subscribeAcknowledge(mqttSubscribeMessage.messageId(), grantedQosLevels);
    }

    public void unsubscribeManager(MqttUnsubscribeMessage mqttUnsubscribeMessage, MqttEndpoint endpoint) {
        System.out.println("客户端取消订阅  【主题】:" + mqttUnsubscribeMessage.topics().toString() + " 【MessageID】:" + mqttUnsubscribeMessage.messageId());
        for (String topic : mqttUnsubscribeMessage.topics()) {
            topicManager.unSubscribe(new Topic(topic), endpoint);
        }

        endpoint.unsubscribeAcknowledge(mqttUnsubscribeMessage.messageId());
    }

    public void disconnectManager(MqttEndpoint endpoint) {
        System.out.println("客户端【Disconnect】主动断开连接");
        System.out.println("用户【" + endpoint.clientIdentifier() + "】断开连接  【isConnect】:" + endpoint.isConnected()); // true
    }

    public void close(MqttEndpoint endpoint) {
        clientID.remove(endpoint.clientIdentifier());
        System.out.println("用户【" + endpoint.clientIdentifier() + "】Close  【isConnect】:" + endpoint.isConnected()); // false
        // 发布遗嘱
        if (!StringUtil.isNullOrEmpty(endpoint.will().willTopic())) {
            topicManager.sendWill(new Topic(endpoint.will().willTopic(), MqttQoS.valueOf(endpoint.will().willQos()), Buffer.buffer(endpoint.will().willMessage()), false, endpoint.will().isWillRetain()));
        }
    }

    public void setListener(AsyncResult<MqttServer> ar) {
        // 监听事件  ar -- new Handler<AsyncResult<MqttServer>>()
        if (ar.succeeded()) {
            System.out.println("MQTT server is listening on port " + ar.result().actualPort());
        } else {
            System.out.println("Error on starting the server");
            ar.cause().printStackTrace();
        }
    }

    public void pingManager(MqttEndpoint endpoint) {
        System.out.println("客户端心跳  【时间】:" + new Timestamp(System.currentTimeMillis()) + " 【ID】:" + endpoint.clientIdentifier() + " 【AutoKeepAlive】:" + endpoint.isAutoKeepAlive() + " 【isConnected】:" + endpoint.isConnected());
        if (!endpoint.isAutoKeepAlive()) {
            endpoint.pong();
        }
    }

    public void exception(MqttEndpoint endpoint, Throwable throwable) {
        System.out.println("客户端【" + endpoint.clientIdentifier() + "】异常断开" + throwable);
    }
}
