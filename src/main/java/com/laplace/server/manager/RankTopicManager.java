package com.laplace.server.manager;

import com.laplace.server.bean.RankTopics;
import com.laplace.server.bean.Topic;
import io.vertx.mqtt.MqttEndpoint;

import java.util.List;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/16 16:28
 * @Info: 订阅、取消订阅、发布
 * @Email:
 */
public class RankTopicManager {

    private List<RankTopics> topTopics;


    // 订阅之前时要校验格式
    public boolean topicsValidate(Topic topic) {

        return false;
    }

    // 处理订阅
    public boolean subscribe(/*Topic topic, 待定*/ MqttEndpoint endpoint) {

        return false;
    }

    // 处理取消订阅
    public boolean unsubscribe(MqttEndpoint endpoint) {

        return false;
    }

    // 发布信息
    public int publish(Topic topic) {

        return 0;
    }


}
