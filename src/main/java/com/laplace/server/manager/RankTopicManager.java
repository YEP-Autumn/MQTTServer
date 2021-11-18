package com.laplace.server.manager;

import com.laplace.server.bean.MqttEndpointPower;
import com.laplace.server.core.RankTopic;
import com.laplace.server.bean.Topic;
import com.laplace.server.utils.TopicUtils;
import io.vertx.mqtt.MqttEndpoint;

import javax.xml.ws.Endpoint;
import java.util.LinkedList;
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
    public void subscribe(Topic topic, MqttEndpoint endpoint) {
        topTopic.subscribe(topic.getTopicName(), endpoint.clientIdentifier());
    }

    // 处理取消订阅
    public void unsubscribe(String topicName, MqttEndpoint endpoint) {
        topTopic.unSubscribe(topicName, endpoint.clientIdentifier());
    }

    // 发布信息
    public int publish(Topic topic) {
        LinkedList<String> subscribeEndpointPowerLis = topTopic.getSubscribeEndpointPowerLis(topic.getTopicName(), new LinkedList<>());
        System.out.println("设备数量：" + subscribeEndpointPowerLis.size());
        subscribeEndpointPowerLis.forEach(new Consumer<String>() {
            @Override
            public void accept(String clientIdentifier) {
                MqttEndpointPower endpoint = EndpointManager.getEndpointByClientIdentifier(clientIdentifier);
                if (topic.getQos().value() > endpoint.getQoS().value()) {
                    topic.setQos(endpoint.getQoS());
                }

                TopicUtils.PUBLISH_DISPATCHER(topic, endpointPower.getEndpoint());
            }
        });
        return 0;
    }


}
