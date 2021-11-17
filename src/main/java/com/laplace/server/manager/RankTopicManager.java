package com.laplace.server.manager;

import com.laplace.server.bean.MqttEndpointPower;
import com.laplace.server.bean.RankTopic;
import com.laplace.server.bean.Topic;
import com.laplace.server.utils.TopicUtils;
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
    public void subscribe(Topic topic, MqttEndpoint endpoint) {

        MqttEndpointPower endpointPower = new MqttEndpointPower();
        endpointPower.setEndpoint(endpoint);
        endpointPower.setQoS(topic.getQos());
        topTopic.subscribe(topic.getTopicName(), endpointPower);

    }

    // 处理取消订阅
    public void unsubscribe(Topic topic, MqttEndpoint endpoint) {

        MqttEndpointPower endpointPower = new MqttEndpointPower();
        endpointPower.setEndpoint(endpoint);
        topTopic.unSubscribe(topic.getTopicName(), endpointPower);

    }

    // 发布信息
    public int publish(Topic topic) {
        LinkedList<MqttEndpointPower> subscribeEndpointPowerLis = topTopic.getSubscribeEndpointPowerLis(topic.getTopicName(), new LinkedList<>());
        System.out.println("设备数量：" + subscribeEndpointPowerLis.size());
        subscribeEndpointPowerLis.forEach(new Consumer<MqttEndpointPower>() {
            @Override
            public void accept(MqttEndpointPower endpointPower) {

                if (topic.getQos().value() > endpointPower.getQoS().value()) {
                    topic.setQos(endpointPower.getQoS());
                }

                TopicUtils.PUBLISH_DISPATCHER(topic, endpointPower.getEndpoint());
            }
        });
        return 0;
    }


}
