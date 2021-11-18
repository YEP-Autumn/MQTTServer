package com.laplace.server.manager;

import com.laplace.server.bean.MqttEndpointPower;
import com.laplace.server.bean.Topic;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.mqtt.MqttEndpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/18 14:36
 * @Info:
 * @Email:
 */
public class EndpointTopicsManagement {

    // 客户端 身份ID---订阅的主题
    private static final HashMap<String, LinkedList<Topic>> subscribeTopics = new HashMap<>();


    public static void addSubscribeTopics(String clientIdentifier, Topic topic) {
        LinkedList<Topic> topics;
        if (subscribeTopics.containsKey(clientIdentifier)) {
            topics = subscribeTopics.get(clientIdentifier);

            int position = topics.indexOf(topic);
            if (position == -1) {
                // 如果不存在直接添加
                topics.add(topic);
            } else if (topic.getQos().value() > topics.get(position).getQos().value()) {
                // 如果存在且服务质量比之前的服务质量大
                topics.get(position).setQos(topic.getQos());
            }

        } else {
            topics = new LinkedList<>();
            topics.add(topic);
            subscribeTopics.put(clientIdentifier, topics);
        }
    }

    public static void removeSubscribeTopics(String clientIdentifier, Topic topic) {

        if (subscribeTopics.containsKey(clientIdentifier)) {
            LinkedList<Topic> list = subscribeTopics.get(clientIdentifier);
            if (list != null) list.remove(topic);
        }
    }

    public static List<Topic> removeAllSubscribeTopics(String clientIdentifier) {
        if (subscribeTopics.containsKey(clientIdentifier)) {
            LinkedList<Topic> needRemoveTopics = subscribeTopics.get(clientIdentifier);
            subscribeTopics.remove(clientIdentifier);
            return needRemoveTopics;
        }
        return new ArrayList<>();
    }

    public static MqttQoS getTopicQos(String clientIdentifier, Topic topic) {
        LinkedList<Topic> topics = subscribeTopics.get(clientIdentifier);
        if (topics != null) {
            int position = topics.indexOf(topic);
            if (position != -1) {
                return topics.get(position).getQos();
            }
        }
        System.out.println("出现逻辑错误,该设备(" + clientIdentifier + ")没有订阅该主题(" + topic.getTopicName() + ")");
        return MqttQoS.valueOf(0);
    }


    private static final HashMap<String, MqttEndpointPower> endpoints = new HashMap<>();

    public static MqttEndpointPower getEndpointByClientIdentifier(String clientIdentifier) {
        return endpoints.get(clientIdentifier);
    }


    /**
     * 添加Endpoint
     * 【偷天换日】
     *
     * @param endpoint
     * @return 如果替换了原来的Endpoint则返回true, 如果没有替换则返回false
     */
    public static boolean addEndpoint(MqttEndpoint endpoint) {
        boolean isReplace = false;
        if (endpoints.containsKey(endpoint.clientIdentifier())) {
            MqttEndpoint oldEndpoint = endpoints.get(endpoint.clientIdentifier()).getEndpoint();
            if (oldEndpoint.isConnected()) oldEndpoint.close();
            isReplace = true;
        }
        endpoints.put(endpoint.clientIdentifier(), new MqttEndpointPower(endpoint));
        return isReplace;
    }

    public static boolean removeEndpoint(MqttEndpoint endpoint) {
        if (endpoints.containsKey(endpoint.clientIdentifier())) {
            endpoints.remove(endpoint.clientIdentifier());
            return true;
        }
        return false;
    }
}
