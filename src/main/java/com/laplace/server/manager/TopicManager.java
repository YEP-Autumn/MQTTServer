package com.laplace.server.manager;

import com.laplace.server.bean.Topic;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Handler;
import io.vertx.mqtt.MqttEndpoint;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/15 12:59
 * @Info:
 * @Email:
 */
public class TopicManager {

     HashMap<Topic, HashSet<MqttEndpoint>> topicEndpoint = new HashMap<>();


    public void subscribe(Topic topic, MqttEndpoint endpoint) {
        HashSet<MqttEndpoint> mqttEndpoints = topicEndpoint.get(topic);
        if (mqttEndpoints == null) mqttEndpoints = new HashSet<>();
        mqttEndpoints.add(endpoint);
        topicEndpoint.put(topic, mqttEndpoints);
    }

    public void unSubscribe(Topic topic, MqttEndpoint endpoint) {
        for (int i = 0; i < 3; i++) {
            topic.setQos(MqttQoS.valueOf(i));
            HashSet<MqttEndpoint> mqttEndpoints = topicEndpoint.get(topic);
            if (mqttEndpoints == null) continue;
            if (mqttEndpoints.size() == 0) continue;
            if (!mqttEndpoints.contains(endpoint)) continue;
            mqttEndpoints.remove(endpoint);
            topicEndpoint.put(topic, mqttEndpoints);
        }
    }

    public  void sendTopic(Topic topic) {
        for (int i = 0; i < 3; i++) {
            HashSet<MqttEndpoint> endpoints = topicEndpoint.get(new Topic(topic.getTopicName(), MqttQoS.valueOf(i)));
            if (endpoints == null) continue;
            if (endpoints.size() == 0) continue;
            topic.setQos(topic.getQos().value() < i ? topic.getQos() : MqttQoS.valueOf(i));  // 服务降级
            int finalI = i;
            endpoints.forEach(new Consumer<MqttEndpoint>() {
                @Override
                public void accept(MqttEndpoint endpoint) {
                    if (!endpoint.isConnected()) {
                        endpoints.remove(endpoint);
                        topicEndpoint.put(new Topic(topic.getTopicName(), MqttQoS.valueOf(finalI)), endpoints);
                        return;
                    }
                    if (endpoint.isCleanSession())
                        topic.setQos(MqttQoS.AT_MOST_ONCE);  // 如果客户端 cleanSession=true 则以最低的服务质量向其发送消息
                    PUBLISH_DISPATCHER(topic, endpoint);
                }
            });
        }
    }

    public void sendWill(Topic topic) {

    }


    public  MqttEndpoint PUBLISH_DISPATCHER(Topic topic, MqttEndpoint endpoint) {
        if (MqttQoS.AT_MOST_ONCE.equals(topic.getQos())) {
            PUBLISH_ALL_QoS(topic, endpoint);
            return endpoint;
        }
        if (MqttQoS.AT_LEAST_ONCE.equals(topic.getQos())) {
            PUBLISH_AT_LEAST_ONCE(topic, endpoint);
            return endpoint;
        }
        if (MqttQoS.EXACTLY_ONCE.equals(topic.getQos())) {
            PUBLISH_EXACTLY_ONCE(topic, endpoint);
            return endpoint;
        }
        return endpoint;
    }


    public MqttEndpoint PUBLISH_ALL_QoS(Topic topic, MqttEndpoint endpoint) {
        System.out.println("发布");
        return endpoint.publish(topic.getTopicName(), topic.getPayload(), topic.getQos(), topic.isDup(), topic.isRetain());
    }


    public MqttEndpoint PUBLISH_AT_LEAST_ONCE(Topic topic, MqttEndpoint endpoint) {
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                final boolean[] isReceived = {false};
                final int[] count = {0};
                while (!isReceived[0] && count[0] < 100) {
                    PUBLISH_ALL_QoS(topic, endpoint).publishAcknowledgeHandler(new Handler<Integer>() {
                        @Override
                        public void handle(Integer integer) {
                            System.out.println("收到Acknowledge的确认消息");
                            isReceived[0] = true;
                        }
                    });
                    Thread.sleep(10000);
                    count[0]++;
                    topic.setDup(true);
                }
            }
        }).start();

        return endpoint;
    }

    public MqttEndpoint PUBLISH_EXACTLY_ONCE(Topic topic, MqttEndpoint endpoint) {
        System.out.println("转发服务质量为2的消息");
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                final boolean[] isCompleted = {false};
                final int[] count = {0};
                while (!isCompleted[0] && count[0] < 100) {
                    PUBLISH_ALL_QoS(topic, endpoint).publishReceivedHandler(new Handler<Integer>() {
                        @Override
                        public void handle(Integer integer) {
                            endpoint.publishRelease(integer).publishCompleteHandler(new Handler<Integer>() {
                                @Override
                                public void handle(Integer integer) {
                                    System.out.println("收到Completed的确认消息");
                                    isCompleted[0] = true;
                                }
                            });
                        }
                    });
                    Thread.sleep(10000);
                    count[0]++;
                    topic.setDup(true);
                }
            }
        }).start();
        return endpoint;
    }

}
