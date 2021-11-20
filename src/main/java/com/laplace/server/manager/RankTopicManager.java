package com.laplace.server.manager;

import com.laplace.server.bean.MqttEndpointPower;
import com.laplace.server.core.RankTopic;
import com.laplace.server.bean.Topic;
import com.laplace.server.utils.TopicUtils;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.mqtt.MqttEndpoint;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/16 16:28
 * @Info: 订阅、取消订阅、发布
 * @Email:
 */
public class RankTopicManager {

    private RankTopic topTopic = new RankTopic("$");

    // 处理订阅
    public void subscribe(Topic topic, String clientIdentifier) {

        // 处理订阅
        topTopic.subscribe(topic.getTopicName(), clientIdentifier);

        // 处理订阅的保留消息
        LinkedList<Topic> topicRetain = topTopic.getTopicRetain(topic.getTopicName(), new LinkedList<>());
        System.out.println("保留消息总数量:" + topicRetain.size());
        topicRetain.forEach(new Consumer<Topic>() {
            @Override
            public void accept(Topic t) {
                t.setRetain(true);
                t.setQos(t.getQos().value() < topic.getQos().value() ? t.getQos() : topic.getQos());  // 服务质量降级
                TopicUtils.PUBLISH_DISPATCHER(t, EndpointTopicsManagement.getEndpointByClientIdentifier(clientIdentifier).getEndpoint());
            }
        });
    }

    // 处理取消订阅
    public void unsubscribe(String topicName, String clientIdentifier) {
        topTopic.unSubscribe(topicName, clientIdentifier);
    }

    // 发布信息
    public int publish(Topic topic) {

        LinkedList<String> subscribeEndpointPowerLis = topTopic.getSubscribeEndpointPowerLis(topic.getTopicName(), new LinkedList<>());
        HashSet<String> subscribeSet = new HashSet<String>(subscribeEndpointPowerLis);
        System.out.println("订阅主题【" + topic.getTopicName() + "】设备数量：" + subscribeSet.size());
        subscribeSet.forEach(new Consumer<String>() {
            @Override
            public void accept(String clientIdentifier) {
                MqttEndpointPower endpoint = EndpointTopicsManagement.getEndpointByClientIdentifier(clientIdentifier);
                // 服务质量降级
                MqttQoS topicQos = EndpointTopicsManagement.getTopicQos(clientIdentifier, topic);
                if (topic.getQos().value() > topicQos.value()) {
                    topic.setQos(topicQos);
                }
                TopicUtils.PUBLISH_DISPATCHER(topic, endpoint.getEndpoint());
            }
        });
        return 0;
    }


    public void unsubscribe(List<Topic> topics, String clientIdentifier) {
        for (Topic topicName : topics) {
            topTopic.unSubscribe(topicName.getTopicName(), clientIdentifier);
        }
    }

    /**
     * 修改保留消息，自动过滤非保留消息
     *
     * @param topic
     */
    public void changeRetain(Topic topic) {
        if (topic.isRetain()) {
            topTopic.changeRetain(topic.getTopicName(), topic);
        }
    }
}
