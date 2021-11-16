package com.laplace.server.bean;

import io.vertx.mqtt.MqttEndpoint;

import java.util.List;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/16 16:25
 * @Info:
 * @Email:
 */
public class RankTopics {

    // 该层级的名字
    private String topic;

    // 当前的深度
    private String currentTopics;

    // 子层级
    private List<RankTopics> subTopics;

    // +通配符
    private RankTopics WildcardOne;

    // #通配符
    private List<MqttEndpoint> wildcardAll;

    // 订阅该层级主题的设备
    private List<MqttEndpointPower> endpoints;

    private boolean isRetain;

    private Topic retain;

}
