package com.laplace.server.utils;

import com.laplace.server.bean.Topic;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Handler;
import io.vertx.mqtt.MqttEndpoint;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/15 12:59
 * @Info:
 * @Email:
 */
public class TopicUtils {

    // 离线消息队列
    private final static HashMap<String, LinkedList<Topic>> offlineTopic = new HashMap<>();


    // 添加离线消息
    public static void addOfflineTopic(String clientIdentifier, Topic topic) {
        LinkedList<Topic> topics;
        if (offlineTopic.containsKey(clientIdentifier)) {
            topics = offlineTopic.get(clientIdentifier);
        } else {
            topics = new LinkedList<>();
        }
        topics.add(topic);
        offlineTopic.put(clientIdentifier, topics);
    }

    // 是否有离线消息
    public static boolean hasOfflineTopic(MqttEndpoint endpoint) {
        return offlineTopic.containsKey(endpoint.clientIdentifier());
    }

    // 发送离线消息
    public static void publishOfflineTopic(MqttEndpoint endpoint) {
        offlineTopic.get(endpoint.clientIdentifier()).forEach(new Consumer<Topic>() {
            @Override
            public void accept(Topic topic) {
                PUBLISH_DISPATCHER(topic, endpoint);
            }
        });
        offlineTopic.remove(endpoint.clientIdentifier());
    }

    public static MqttEndpoint PUBLISH_DISPATCHER(Topic topic, MqttEndpoint endpoint) {
        // 如果客户端是连接状态则直接发送
        if (endpoint.isConnected()) {
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
        // 如果客户端的cleanSession为true 说明需要清理会话 不用保存离线消息
        // PS:因为客户端close时，取消了需要清理会话的客户端的订阅，所有出现这种情况是意外事件，可能程序逻辑有问题
        if (endpoint.isCleanSession()) {
            System.out.println("有不用保存离线消息的客户端断线,订阅消息没有及时清空！！！");
            return endpoint;
        }
        addOfflineTopic(endpoint.clientIdentifier(), topic);
        return endpoint;
    }


    /**
     * 最终发送消息的方法
     *
     * @param topic
     * @param endpoint
     * @return
     */
    private static MqttEndpoint PUBLISH_ALL_QoS(Topic topic, MqttEndpoint endpoint) {
        return endpoint.publish(topic.getTopicName(), topic.getPayload(), topic.getQos(), topic.isDup(), topic.isRetain());
    }


    /**
     * 发布服务质量为1的消息
     *
     * @param topic
     * @param endpoint
     * @return
     */
    private static MqttEndpoint PUBLISH_AT_LEAST_ONCE(Topic topic, MqttEndpoint endpoint) {
        System.out.println("转发服务质量为AT_LEAST_ONCE的消息");
        PUBLISH_ALL_QoS(topic, endpoint).publishAcknowledgeHandler(new Handler<Integer>() {
            @Override
            public void handle(Integer integer) {
                System.out.println("收到客户端【" + endpoint.clientIdentifier() + "】报文标识为【" + integer + "】的Acknowledge确认消息");
            }
        });
        return endpoint;
    }


    /**
     * 发布服务质量为2的消息
     *
     * @param topic
     * @param endpoint
     * @return
     */
    private static MqttEndpoint PUBLISH_EXACTLY_ONCE(Topic topic, MqttEndpoint endpoint) {
        System.out.println("转发服务质量为EXACTLY_ONCE的消息");
        PUBLISH_ALL_QoS(topic, endpoint).publishReceivedHandler(new Handler<Integer>() {
            @Override
            public void handle(Integer integer) {
                endpoint.publishRelease(integer).publishCompleteHandler(new Handler<Integer>() {
                    @Override
                    public void handle(Integer integer) {
                        System.out.println("收到客户端【" + endpoint.clientIdentifier() + "】报文标识为【" + integer + "】的Completed确认消息");
                    }
                });
            }
        });
        return endpoint;
    }

//    private static MqttEndpoint PUBLISH_AT_LEAST_ONCE(Topic topic, MqttEndpoint endpoint) {
//        System.out.println("转发服务质量为AT_LEAST_ONCE的消息");
//        new Thread(new Runnable() {
//            @SneakyThrows
//            @Override
//            public void run() {
//                final boolean[] isReceived = {false};
//                final int[] count = {0};
//                while (!isReceived[0] && count[0] < 100) {
//                    PUBLISH_ALL_QoS(topic, endpoint).publishAcknowledgeHandler(new Handler<Integer>() {
//                        @Override
//                        public void handle(Integer integer) {
////                            endpoint.publishAcknowledge(integer);
//                            System.out.println("收到客户端【" + endpoint.clientIdentifier() + "】报文标识为【" + integer + "】的Acknowledge确认消息");
//                            isReceived[0] = true;
//                        }
//                    });
//                    Thread.sleep(10000);
//                    count[0]++;
//                    topic.setDup(true);
//                }
//            }
//        }).start();
//
//        return endpoint;
//    }
//
//    private static MqttEndpoint PUBLISH_EXACTLY_ONCE(Topic topic, MqttEndpoint endpoint) {
//        System.out.println("转发服务质量为EXACTLY_ONCE的消息");
//        new Thread(new Runnable() {
//            @SneakyThrows
//            @Override
//            public void run() {
//                final boolean[] isCompleted = {false};
//                final int[] count = {0};
//                while (!isCompleted[0] && count[0] < 100) {
//                    PUBLISH_ALL_QoS(topic, endpoint).publishReceivedHandler(new Handler<Integer>() {
//                        @Override
//                        public void handle(Integer integer) {
//                            endpoint.publishRelease(integer).publishCompleteHandler(new Handler<Integer>() {
//                                @Override
//                                public void handle(Integer integer) {
//                                    System.out.println("收到客户端【" + endpoint.clientIdentifier() + "】报文标识为【" + integer + "】的Completed确认消息");
//                                    isCompleted[0] = true;
//                                }
//                            });
//                        }
//                    });
//                    Thread.sleep(10000);
//                    count[0]++;
//                    topic.setDup(true);
//                }
//            }
//        }).start();
//        return endpoint;
//    }

    // 订阅之前时要校验格式
    public static boolean topicsValidate(Topic topic) {
        if ("#".equals(topic.getTopicName()) || "+".equals(topic.getTopicName())) return true;
        String replace = topic.getTopicName();
        if (replace.contains("+")) {
            // 去除符合条件的+

            while (replace.contains("/+/")) {
                replace = replace.replaceAll("/\\+/", "//");
            }
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
