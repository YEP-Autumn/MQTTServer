package com.laplace.server.manager;

import com.laplace.server.bean.MqttEndpointPower;
import com.laplace.server.core.RankTopic;
import com.laplace.server.bean.Topic;
import com.laplace.server.utils.TopicUtils;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.mqtt.MqttEndpoint;

import java.util.LinkedList;
import java.util.List;
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
        topTopic.subscribe(topic.getTopicName(), clientIdentifier);
    }

    // 处理取消订阅
    public void unsubscribe(String topicName, String clientIdentifier) {
        topTopic.unSubscribe(topicName, clientIdentifier);
    }

    // 发布信息
    public int publish(Topic topic) {
        LinkedList<String> subscribeEndpointPowerLis = topTopic.getSubscribeEndpointPowerLis(topic.getTopicName(), new LinkedList<>());
        System.out.println("设备数量：" + subscribeEndpointPowerLis.size());
        subscribeEndpointPowerLis.forEach(new Consumer<String>() {
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
}
