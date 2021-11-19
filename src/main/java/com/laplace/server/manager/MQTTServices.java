package com.laplace.server.manager;

import com.laplace.server.bean.Topic;
import com.laplace.server.utils.TopicUtils;
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
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/16 14:52
 * @Info:
 * @Email:
 */

public class MQTTServices {


    RankTopicManager rankTopicManager = new RankTopicManager();


    public boolean dealWithLogin(MqttEndpoint endpoint) {
        // 接受客户端连接----如果集合中存在这个ID-----偷天换日
        if (EndpointTopicsManagement.addEndpoint(endpoint)) {
            System.out.println("有新客户端连接--成功替换");
        } else {
            System.out.println("有新客户端连接--添加成功");
        }

        // 打印用户登录信息
        System.out.println("【ClientID】:" + endpoint.clientIdentifier() + " 【连接状态】" + endpoint.isConnected() + " 【CleanSession】" + endpoint.isCleanSession());

        // 处理客户端的用户名密码
        if (endpoint.auth() == null) {
            // 如果没有密码拒绝连接
            System.out.println("用户连接没有携带用户名密码");
            endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED);
            return false;
        }

        // 打印用户名密码信息
        System.out.println("[username = " + endpoint.auth().userName() + ", password = " + endpoint.auth().password() + "]");
        if (!("2017248646".equals(endpoint.auth().userName()) || "2017248646Ss.".equals(endpoint.auth().password()))) {
            // 如果密码错误拒绝连接
            System.out.println("不支持的用户名密码");
            endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
            return false;
        }

        // 接收用户连接请求
        endpoint.autoKeepAlive(false);  // 自动保持连接,默认为true

        // 如果客户端需要干净的会话，清理之前客户端的残留
        if (endpoint.isCleanSession()) {
            endpoint.accept(false);
            rankTopicManager.unsubscribe(EndpointTopicsManagement.removeAllSubscribeTopics(endpoint.clientIdentifier()), endpoint.clientIdentifier());
            return true;
        }
        // 是否存在未确认的会话
        boolean hasOfflineTopic = TopicUtils.hasOfflineTopic(endpoint);
        endpoint.accept(hasOfflineTopic);
        if (hasOfflineTopic) {
            TopicUtils.publishOfflineTopic(endpoint);
        }
        return true;
    }


    public void subscribeManager(MqttSubscribeMessage mqttSubscribeMessage, MqttEndpoint endpoint) {

        List<MqttQoS> grantedQosLevels = new ArrayList<>();
        for (MqttTopicSubscription s : mqttSubscribeMessage.topicSubscriptions()) {
            System.out.println("【订阅主题】:" + s.topicName() + " 【服务质量】:" + s.qualityOfService());
            grantedQosLevels.add(s.qualityOfService());
            Topic topic = new Topic(s.topicName(), s.qualityOfService());
            if (!TopicUtils.topicsValidate(topic)) {
                endpoint.close(); // 如果订阅主题不符合规定直接关闭连接
                return;
            }
            rankTopicManager.subscribe(topic, endpoint.clientIdentifier());
            EndpointTopicsManagement.addSubscribeTopics(endpoint.clientIdentifier(), new Topic(topic.getTopicName(), s.qualityOfService()));  // 保存该endpoint订阅过的主题
        }
        endpoint.subscribeAcknowledge(mqttSubscribeMessage.messageId(), grantedQosLevels);

    }


    public void unsubscribeManager(MqttUnsubscribeMessage mqttUnsubscribeMessage, MqttEndpoint endpoint) {
        System.out.println("客户端取消订阅  【主题】:" + mqttUnsubscribeMessage.topics().toString() + " 【MessageID】:" + mqttUnsubscribeMessage.messageId());
        for (String topicName : mqttUnsubscribeMessage.topics()) {
            Topic topic = new Topic(topicName);
            rankTopicManager.unsubscribe(topic.getTopicName(), endpoint.clientIdentifier());
            EndpointTopicsManagement.removeSubscribeTopics(endpoint.clientIdentifier(), new Topic(topic.getTopicName()));
        }
        endpoint.unsubscribeAcknowledge(mqttUnsubscribeMessage.messageId());
    }


    public void publishManager(MqttPublishMessage mqttPublishMessage, MqttEndpoint endpoint) {
        // endpoint.publishAutoAck(true);  // 自动处理响应
        System.out.println("客户端发布主题   【主题】:" + mqttPublishMessage.topicName() +
                " 【服务质量】:" + mqttPublishMessage.qosLevel() +
                " 【Message】:" + mqttPublishMessage.payload().toString() +
                " 【保留】:" + mqttPublishMessage.isRetain() +
                " 【重复】:" + mqttPublishMessage.isDup());
        // 如果是重复消息(isDup=true)说明该客户端网络不好，曾经发送过数据但没有收到返回消息
        Topic topic = new Topic(mqttPublishMessage.topicName(), mqttPublishMessage.qosLevel(), mqttPublishMessage.payload(), false, mqttPublishMessage.isRetain());

        // 必须先修改保留消息:
        // 如果后修改保留消息，可能出现，客户端发送保留消息时，服务端获取完订阅该主题设备后，有设备订阅了该主题。
        // 此时，订阅的客户端即没有收到保留消息，服务端也没有给订阅客户端发送消息
        // 反之，客户端可能收到两条消息: 第一条保留消息，第二条即时消息(可能性非常小)
        rankTopicManager.changeRetain(topic);
        topic.setRetain(false);

        // QoS = 0
        if (mqttPublishMessage.qosLevel() == MqttQoS.AT_MOST_ONCE) {
            rankTopicManager.publish(topic);
            return;
        }

/**
 *    QoS = 1
 *                (PUBLISH)
 *    publisher   ---------->  Broker
 *                (PUBACK)
 *    publisher   <----------  Broker
 */
        if (mqttPublishMessage.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
            endpoint.publishAcknowledge(mqttPublishMessage.messageId());
            rankTopicManager.publish(topic);
            return;
        }

/**
 *
 *    QoS = 2
 *                (PUBLISH)
 *    publisher   ---------->  Broker
 *                (PUBREC)
 *    publisher   <----------  Broker
 *                (PUBREL)
 *    publisher   ---------->  Broker
 *                (PUBCOMP)
 *    publisher   <----------  Broker
 *
 */
        if (mqttPublishMessage.qosLevel() == MqttQoS.EXACTLY_ONCE) {
            endpoint.publishReceived(mqttPublishMessage.messageId()).publishReleaseHandler(new Handler<Integer>() {
                @Override
                public void handle(Integer integer) {
                    endpoint.publishComplete(integer);
                    rankTopicManager.publish(topic);
                }
            });
        }
    }


    public void disconnectManager(MqttEndpoint endpoint) {
        System.out.println("客户端【" + endpoint.clientIdentifier() + "】 Disconnect 主动断开连接");
        EndpointTopicsManagement.disconnect(endpoint.clientIdentifier());
//        System.out.println("用户【" + endpoint.clientIdentifier() + "】断开连接  【isConnect】:" + endpoint.isConnected()); // true
//        System.out.println("遗嘱:" + endpoint.will().isWillFlag());

    }

    public void close(MqttEndpoint endpoint) {
        System.out.println("客户端【" + endpoint.clientIdentifier() + "】连接关闭");
        if (EndpointTopicsManagement.isNeedSendWill(endpoint.clientIdentifier()) && endpoint.will().isWillFlag() && !StringUtil.isNullOrEmpty(endpoint.will().willTopic())) {
            Topic topic = new Topic(endpoint.will().willTopic(), MqttQoS.valueOf(endpoint.will().willQos()), Buffer.buffer(endpoint.will().willMessage()), false, endpoint.will().isWillRetain());
            rankTopicManager.publish(topic);
            System.out.println("发送遗嘱:" + topic);
        }
        if (endpoint.isCleanSession()) {
            System.out.println("客户端需要清理会话");
            EndpointTopicsManagement.removeAllSubscribeTopics(endpoint.clientIdentifier());  // 正常断线 且清理回话为true，删除所有订阅信息
            EndpointTopicsManagement.removeEndpoint(endpoint.clientIdentifier());
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

    // 处理客户端心跳请求
    public void pingManager(MqttEndpoint endpoint) {
        if (endpoint.isAutoKeepAlive()) {
            return;
        }
        // 如果服务端没有开启自动保持心跳  则发送保持心跳响应包
        endpoint.pong();
    }

    public void exception(MqttEndpoint endpoint, Throwable throwable) {
        System.out.println("客户端【" + endpoint.clientIdentifier() + "】异常断开" + throwable);
    }

}
