package com.laplace.server.bean;

import lombok.Data;

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

//    // 当前的深度
//    private String currentTopics;

    // 子层级
    private LinkedList<RankTopic> subTopics;

    // +通配符层级
    private RankTopic wildcardOne;

    // #通配符设备
    private LinkedList<MqttEndpointPower> wildcardAll;

    // 订阅该层级主题的设备
    private LinkedList<MqttEndpointPower> endpoints;

    // 该层级的主题是否有保留消息
    private boolean isRetain;

    // 该层级主题的保留消息
    private Topic retain;

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
    public void subscribe(String topic, MqttEndpointPower endpointPower) {
        // 如果topic为#通配符将其加入该层级下的#通配符设备中
        if ("#".equals(topic)) {
            // 查找这个设备是否订阅过该层级主题
            int positionW = this.wildcardAll.indexOf(endpointPower);
            if (positionW == -1) {
                // 如果没有直接添加
                this.wildcardAll.add(endpointPower);
                return;
            }
            // 如果订阅过  使用最高质量的订阅
            MqttEndpointPower power = this.wildcardAll.get(positionW);
            power.setQoS(power.getQoS().value() > endpointPower.getQoS().value() ? power.getQoS() : endpointPower.getQoS());
            return;
        }
        if ("+".equals(topic)) {

            if (this.wildcardOne == null) {
                this.wildcardOne = new RankTopic("+");
                this.wildcardOne.getEndpoints().add(endpointPower);
                return;
            }

            LinkedList<MqttEndpointPower> subEndpoints = this.wildcardOne.getEndpoints();
            int positionWO = subEndpoints.indexOf(endpointPower);
            if (positionWO == -1) {
                subEndpoints.add(endpointPower);
                return;
            }
            MqttEndpointPower power = this.subTopics.get(positionWO).getEndpoints().get(positionWO);
            power.setQoS(power.getQoS().value() > endpointPower.getQoS().value() ? power.getQoS() : endpointPower.getQoS());
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
            LinkedList<MqttEndpointPower> subEndpoints = this.subTopics.get(position).getEndpoints();
            // 查找这个设备有没有订阅过该层级主题
            int positionE = subEndpoints.indexOf(endpointPower);
            if (positionE == -1) {
                // 如果没有直接向这个层级主题中添加该设备
                System.out.println("没有订阅过");
                subEndpoints.add(endpointPower);
                return;
            }
            // 如果订阅过  使用最高质量的订阅
            MqttEndpointPower power = this.subTopics.get(position).getEndpoints().get(positionE);
            power.setQoS(power.getQoS().value() > endpointPower.getQoS().value() ? power.getQoS() : endpointPower.getQoS());
            System.out.println("订阅过  使用最高质量的订阅" + power.getQoS().value());
            return;

//            subTopics.get(position).getEndpoints().add(endpointPower);
//            this.endpoints.add(endpointPower);
//            return;
        }
        int splitPosition = topic.indexOf("/");
        String thisTopic = topic.substring(0, splitPosition);
        String subTopic = topic.substring(splitPosition + 1);

        if ("+".equals(thisTopic)) {
            if (this.wildcardOne == null) {
                this.wildcardOne = new RankTopic("+");
            }
            this.wildcardOne.subscribe(subTopic, endpointPower);
            return;
        }

        int position = subTopics.indexOf(new RankTopic(thisTopic));
        if (position == -1) {
            RankTopic rankTopic = new RankTopic(thisTopic);
            this.subTopics.add(rankTopic);
            position = this.subTopics.size() - 1;
        }
        this.subTopics.get(position).subscribe(subTopic, endpointPower);

    }

    public void unSubscribe(String topic, MqttEndpointPower endpointPower) {

        if ("#".equals(topic)) {
            System.out.println("#");
            this.wildcardAll.removeFirstOccurrence(endpointPower);
            return;
        }

        if ("+".equals(topic)) {
            System.out.println("+");
            this.wildcardOne.getEndpoints().removeFirstOccurrence(endpointPower);
            return;
        }

        if (!topic.contains("/")) {
            System.out.println("no '/'");
            int position = this.subTopics.indexOf(new RankTopic(topic));
            if (position == -1) return;
            boolean b = this.subTopics.get(position).getEndpoints().removeFirstOccurrence(endpointPower);
            System.out.println(b);
            return;
        }

        int splitPosition = topic.indexOf("/");
        String thisTopic = topic.substring(0, splitPosition);
        String subTopic = topic.substring(splitPosition + 1);
        if ("+".equals(thisTopic)) {
            System.out.println("+/");
            this.wildcardOne.unSubscribe(subTopic, endpointPower);
        }

        int i = this.subTopics.indexOf(new RankTopic(thisTopic));
        if (i == -1) {
            System.out.println("-1         " + thisTopic);
            return;
        }
        System.out.println("next");
        this.subTopics.get(i).unSubscribe(subTopic, endpointPower);
    }


    // 获取需要发布的设备
    public LinkedList<MqttEndpointPower> getSubscribeEndpointPowerLis(String topic, LinkedList<MqttEndpointPower> endpoints) {
        endpoints.addAll(this.wildcardAll);
        System.out.println(topic + "             " + endpoints.size());
        if (!topic.contains("/")) {
            if (this.wildcardOne != null) {
                endpoints.addAll(this.wildcardOne.getEndpoints());
            }


            {   // 添加订阅下一个层级主题的 #
                LinkedList<MqttEndpointPower> finalEndpoints = new LinkedList<>();
                this.subTopics.forEach(new Consumer<RankTopic>() {
                    @Override
                    public void accept(RankTopic rankTopic) {
                        finalEndpoints.addAll(rankTopic.getWildcardAll());
                    }
                });
                endpoints.addAll(finalEndpoints);
                if (this.getWildcardOne() != null) {
                    endpoints.addAll(this.getWildcardOne().getWildcardAll());
                }
            }

            // 添加该主题下的设备
            int position = this.subTopics.indexOf(new RankTopic(topic));
            if (position == -1) return endpoints;
            endpoints.addAll(this.subTopics.get(position).getEndpoints());
            return endpoints;
        }

        int splitPosition = topic.indexOf("/");
        String thisTopic = topic.substring(0, splitPosition);
        String subTopic = topic.substring(splitPosition + 1);

        // 处理 +
        if (this.wildcardOne != null) {
            System.out.println("处理+");
            endpoints = this.wildcardOne.getSubscribeEndpointPowerLis(subTopic, endpoints);
        }

        int subPosition = this.subTopics.indexOf(new RankTopic(thisTopic));
        if (subPosition == -1) return endpoints;
        System.out.println("下一层级");
        return this.subTopics.get(subPosition).getSubscribeEndpointPowerLis(subTopic, endpoints);
    }

}
