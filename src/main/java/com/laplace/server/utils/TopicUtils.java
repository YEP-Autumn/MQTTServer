package com.laplace.server.utils;

import com.laplace.server.bean.Topic;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Handler;
import io.vertx.mqtt.MqttEndpoint;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/15 12:59
 * @Info:
 * @Email:
 */
public class TopicUtils {

//     HashMap<Topic, HashSet<MqttEndpoint>> topicEndpoint = new HashMap<>();

//
//    public void subscribe(Topic topic, MqttEndpoint endpoint) {
//        HashSet<MqttEndpoint> mqttEndpoints = topicEndpoint.get(topic);
//        if (mqttEndpoints == null) mqttEndpoints = new HashSet<>();
//        mqttEndpoints.add(endpoint);
//        topicEndpoint.put(topic, mqttEndpoints);
//    }
//
//    public void unSubscribe(Topic topic, MqttEndpoint endpoint) {
//        for (int i = 0; i < 3; i++) {
//            topic.setQos(MqttQoS.valueOf(i));
//            HashSet<MqttEndpoint> mqttEndpoints = topicEndpoint.get(topic);
//            if (mqttEndpoints == null) continue;
//            if (mqttEndpoints.size() == 0) continue;
//            if (!mqttEndpoints.contains(endpoint)) continue;
//            mqttEndpoints.remove(endpoint);
//            topicEndpoint.put(topic, mqttEndpoints);
//        }
//    }

    public static MqttEndpoint PUBLISH_DISPATCHER(Topic topic, MqttEndpoint endpoint) {
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


    private static MqttEndpoint PUBLISH_ALL_QoS(Topic topic, MqttEndpoint endpoint) {
        System.out.println("发布");
        return endpoint.publish(topic.getTopicName(), topic.getPayload(), topic.getQos(), topic.isDup(), topic.isRetain());
    }


    private static MqttEndpoint PUBLISH_AT_LEAST_ONCE(Topic topic, MqttEndpoint endpoint) {
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

    private static MqttEndpoint PUBLISH_EXACTLY_ONCE(Topic topic, MqttEndpoint endpoint) {
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


    // 订阅之前时要校验格式
    public static boolean topicsValidate(Topic topic) {
        if ("#".equals(topic.getTopicName()) || "+".equals(topic.getTopicName())) return true;
        String replace = topic.getTopicName();
        if (replace.contains("+")) {
            // 去除符合条件的+
            replace = StringUtils.replace(replace, "/+/", "//");
            if (replace.startsWith("+/")) {
                // 去除最前端的   +/
                replace = replace.substring(1);
            }
            if (replace.endsWith("/+")) {
                // 去除最后端的    /+
                replace = replace.substring(0, replace.length() - 1);
            }
            if (replace.contains("+")) {
                //  剩下的 + 就是不符合条件的+
                return false;
            }
        }
        if (replace.contains("#")) {
            // 如果有#  就必须在最后  且只能是/#  且只有一个
            if (replace.endsWith("/#")) {
                // 去除末尾
                replace = replace.substring(0, replace.length() - 1);
            }
            return !replace.contains("#");
        }
        return true;
    }
}
