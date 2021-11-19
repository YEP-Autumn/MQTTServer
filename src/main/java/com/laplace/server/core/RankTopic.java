package com.laplace.server.core;

import com.laplace.server.bean.Topic;
import io.vertx.core.buffer.Buffer;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/16 16:25
 * @Info:
 * @Email:
 */
@Data
public class RankTopic {

    // 该层级的名字
    private String topic;

    // 当前的深度
//    private String currentTopics;

    // 子层级
    private LinkedList<RankTopic> subTopics;

    /*
         + 通配符层级(用于添加订阅此层级的设备)
        订阅时: 会将主题为 + 的层级放入这个层级中,子层级继续正常处理
        发布、获取订阅某主题的设备时: 不管topicName是什么,都会查找一遍 + 主题层级
     */
    private RankTopic wildcardOne;

    // #通配符设备
    private LinkedList<String> wildcardAll;

    // 订阅该层级主题的设备
    private LinkedList<String> endpoints;

    // 该层级的主题是否有保留消息
    private boolean isRetain;

    // 该层级主题的保留消息
    private Topic retainTopic;

    public RankTopic(String topic) {
        this.topic = topic;
        this.subTopics = new LinkedList<>();
        this.wildcardAll = new LinkedList<>();
        this.endpoints = new LinkedList<>();
        this.isRetain = false;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RankTopic that = (RankTopic) o;
        return Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic);
    }


    // 向子主题中添加订阅
    public void subscribe(String topic, String clientIdentifier) {
        // 如果topic为#通配符将其加入该层级下的#通配符设备中
        if ("#".equals(topic)) {
            // 查找这个设备是否订阅过该层级主题
            int positionW = this.wildcardAll.indexOf(clientIdentifier);
            if (positionW == -1) {
                // 如果没有直接添加
                this.wildcardAll.add(clientIdentifier);
                return;
            }
            return;
        }
        if ("+".equals(topic)) {

            if (this.wildcardOne == null) {
                this.wildcardOne = new RankTopic("+");
                this.wildcardOne.getEndpoints().add(clientIdentifier);
                return;
            }

            LinkedList<String> subEndpoints = this.wildcardOne.getEndpoints();
            int positionWO = subEndpoints.indexOf(clientIdentifier);
            if (positionWO == -1) {
                subEndpoints.add(clientIdentifier);
                return;
            }

            return;
        }

        // 如果topic不包含 / 说明已经是最后一个层级主题了
        if (!topic.contains("/")) {
            // 查找子层级是否有这个层级主题
            int position = this.subTopics.indexOf(new RankTopic(topic));
            if (position == -1) {
                // 如果没有创建这个主题
                RankTopic rankTopic = new RankTopic(topic);
                this.subTopics.add(rankTopic);
                position = this.subTopics.size() - 1;
            }
            // 得到订阅了这个层级主题的设备
            LinkedList<String> subEndpoints = this.subTopics.get(position).getEndpoints();
            // 查找这个设备有没有订阅过该层级主题
            if (!subEndpoints.contains(clientIdentifier)) {
                // 如果没有直接向这个层级主题中添加该设备
                subEndpoints.add(clientIdentifier);
                return;
            }
            return;
        }

        // 继续向下索引
        int splitPosition = topic.indexOf("/");
        String thisTopic = topic.substring(0, splitPosition);
        String subTopic = topic.substring(splitPosition + 1);

        // 如果为 + 特殊情况 特殊处理
        if ("+".equals(thisTopic)) {
            if (this.wildcardOne == null) {
                this.wildcardOne = new RankTopic("+");
            }
            this.wildcardOne.subscribe(subTopic, clientIdentifier);
            return;
        }
        // 正常处理 判断是否存在这个层级主题
        int position = subTopics.indexOf(new RankTopic(thisTopic));
        if (position == -1) {
            // 如果不存在 新建一个层级 将position设置为该层级
            RankTopic rankTopic = new RankTopic(thisTopic);
            this.subTopics.add(rankTopic);
            position = this.subTopics.size() - 1;
        }
        // 向下索引
        this.subTopics.get(position).subscribe(subTopic, clientIdentifier);

    }

    public void unSubscribe(String topic, String clientIdentifier) {

        if ("#".equals(topic)) {
            this.wildcardAll.removeFirstOccurrence(clientIdentifier);
            return;
        }

        if ("+".equals(topic)) {
            this.wildcardOne.getEndpoints().removeFirstOccurrence(clientIdentifier);
            return;
        }

        if (!topic.contains("/")) {
            int position = this.subTopics.indexOf(new RankTopic(topic));
            if (position == -1) return;
            this.subTopics.get(position).getEndpoints().removeFirstOccurrence(clientIdentifier);
            return;
        }

        int splitPosition = topic.indexOf("/");
        String thisTopic = topic.substring(0, splitPosition);
        String subTopic = topic.substring(splitPosition + 1);
        if ("+".equals(thisTopic)) {
            this.wildcardOne.unSubscribe(subTopic, clientIdentifier);
        }

        int i = this.subTopics.indexOf(new RankTopic(thisTopic));
        if (i == -1) {
            return;
        }
        this.subTopics.get(i).unSubscribe(subTopic, clientIdentifier);
    }


    // 获取需要发布的设备
    public LinkedList<String> getSubscribeEndpointPowerLis(String topic, LinkedList<String> endpointClientIdentifiers) {
        endpointClientIdentifiers.addAll(this.wildcardAll);
        if (!topic.contains("/")) {
            // 如果不包含 / 说明已经是最后一个层级了
            if (this.wildcardOne != null) {
                // 添加订阅了 + 层级的
                endpointClientIdentifiers.addAll(this.wildcardOne.getEndpoints());
            }

            {   // 添加订阅下一个层级主题的 #
                LinkedList<String> finalEndpoints = new LinkedList<>();
                this.subTopics.forEach(new Consumer<RankTopic>() {
                    @Override
                    public void accept(RankTopic rankTopic) {
                        finalEndpoints.addAll(rankTopic.getWildcardAll());
                    }
                });
                endpointClientIdentifiers.addAll(finalEndpoints);
                if (this.getWildcardOne() != null) {
                    // 添加 + 层级 下一个层级的#
                    endpointClientIdentifiers.addAll(this.getWildcardOne().getWildcardAll());
                }
            }

            // 添加该主题下的设备
            int position = this.subTopics.indexOf(new RankTopic(topic));
            if (position == -1) return endpointClientIdentifiers;
            endpointClientIdentifiers.addAll(this.subTopics.get(position).getEndpoints());
            return endpointClientIdentifiers;
        }

        // 如果不是最终层级继续向下索引(递归查找)
        int splitPosition = topic.indexOf("/");  // 将topic切片,区分出这个层级的子层级和剩余层级   a/b/c  ——>  a 和 b/c
        String thisTopic = topic.substring(0, splitPosition);
        String subTopic = topic.substring(splitPosition + 1);

        // 处理订阅  +  层级的设备
        if (this.wildcardOne != null) {
            endpointClientIdentifiers = this.wildcardOne.getSubscribeEndpointPowerLis(subTopic, endpointClientIdentifiers);
        }

        int subPosition = this.subTopics.indexOf(new RankTopic(thisTopic));
        if (subPosition == -1) return endpointClientIdentifiers;  // 如果没有这个层级 说明没有订阅了这个层级的设备，直接返回索引结果
        return this.subTopics.get(subPosition).getSubscribeEndpointPowerLis(subTopic, endpointClientIdentifiers);  // 如果存在thisTopic这个层级继续向下索引
    }


    // 修改保留消息
    public void changeRetain(String topicName, Topic topic) {
        if (Buffer.buffer("").equals(topic.getPayload())) {
            removeRetain(topicName);
            return;
        }
        updateRetain(topicName, topic);
    }

    /**
     * 删除某主题保留消息
     *
     * @param topicName
     */
    public void removeRetain(String topicName) {
        if (!topicName.contains("/")) {
            // 说明是最终层级
            int position = this.subTopics.indexOf(new RankTopic(topicName));
            if (position != -1) {
                // 如果存在这个子层级 将其保留消息设置为false
                subTopics.get(position).setRetain(false);
            }
            return;
        }
        // 如果不是最终层级
        int position = topicName.indexOf("/");
        String thisTopic = topicName.substring(0, position);
        String subTopic = topicName.substring(position + 1);

        int positionSub = this.subTopics.indexOf(new RankTopic(thisTopic));
        if (positionSub != -1) {
            this.subTopics.get(positionSub).removeRetain(subTopic);
        }
    }

    /**
     * 更新某主题的保留消息
     *
     * @param topicName
     * @param topic
     */
    private void updateRetain(String topicName, Topic topic) {
        // 如果是最终层级
        if (!topicName.contains("/")) {
            // 最终层级处理
            int position = this.subTopics.indexOf(new RankTopic(topicName));
            if (position == -1) {
                RankTopic rankTopic = new RankTopic(topicName);
                rankTopic.setRetain(true);
                rankTopic.setRetainTopic(topic);
                System.out.println(rankTopic);
                this.subTopics.add(rankTopic);
                return;
            }
            this.subTopics.get(position).setRetain(true);
            this.subTopics.get(position).setRetainTopic(topic);
            return;
        }

        // 如果不是最终层级
        int position = topicName.indexOf("/");
        String thisTopic = topicName.substring(0, position);
        String subTopic = topicName.substring(position + 1);

        // 判断是否有thisTopic层级
        int positionSub = this.subTopics.indexOf(new RankTopic(thisTopic));
        if (positionSub == -1) {
            // 没有则创建thisTopic层级
            this.subTopics.add(new RankTopic(thisTopic));
            positionSub = this.subTopics.size() - 1;
        }
        this.subTopics.get(positionSub).updateRetain(subTopic, topic);
    }

    // 获取用户所订阅主题的所有保留消息
    public LinkedList<Topic> getTopicRetain(String topicName, @NonNull LinkedList<Topic> topics) {

        if ("#".equals(topicName)) {
            LinkedList<Topic> temporaryTopics = new LinkedList<>();
            this.subTopics.forEach(new Consumer<RankTopic>() {
                @Override
                public void accept(RankTopic rankTopic) {
                    if (rankTopic.isRetain) {
                        temporaryTopics.add(rankTopic.getRetainTopic());
                    }
                    temporaryTopics.addAll(rankTopic.getTopicRetain(topicName, new LinkedList<>()));
                }
            });
            topics.addAll(temporaryTopics);
            return topics;
        }

        if ("+".equals(topicName)) {

            this.subTopics.forEach(new Consumer<RankTopic>() {
                @Override
                public void accept(RankTopic rankTopic) {
                    if (rankTopic.isRetain) {
                        topics.add(rankTopic.getRetainTopic());
                    }
                }
            });
            return topics;
        }

        if (!topicName.contains("/")) {
            int positionOne = this.subTopics.indexOf(new RankTopic(topicName));
            // 如果有这个主题
            if (positionOne != -1) {
                // 并且这个主题有保留消息
                if (this.subTopics.get(positionOne).isRetain) {
                    topics.add(this.subTopics.get(positionOne).getRetainTopic());
                }
            }
            return topics;
        }

        // 如果不是最终层级
        int position = topicName.indexOf("/");
        String thisTopic = topicName.substring(0, position);
        String subTopic = topicName.substring(position + 1);

        if ("+".equals(thisTopic)) {
            this.subTopics.forEach(new Consumer<RankTopic>() {
                @Override
                public void accept(RankTopic rankTopic) {
                    topics.addAll(rankTopic.getTopicRetain(subTopic, topics));
                }
            });
            return topics;
        }
        int positionTP = this.subTopics.indexOf(new RankTopic(thisTopic));
        if (positionTP == -1) {
            return topics;
        }
        return this.subTopics.get(positionTP).getTopicRetain(subTopic, topics);
    }

}
