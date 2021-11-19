package com.laplace;

import com.laplace.server.bean.Topic;
import com.laplace.server.utils.TopicUtils;

/**
 * @Author: YEP
 * @CreateDate: 2021/11/15 17:41
 * @Info:
 * @Email:
 */
public class PSVM {


    public static void main(String[] args) {

        String topicName = "abc/+/+/+/#";
        topicName = topicName.replaceAll("\\+", ".*?").replaceAll("/#", ".*");
        String test = "abc/sfs/";
        System.out.println(topicName);
        System.out.println(test.matches(topicName));
        System.out.println(test.matches("abc/.*?/.*?/.*?/.*"));



//
//        LinkedList<MqttEndpointPower> a = new LinkedList<>();
//
//        MqttEndpointPower endpointPower = new MqttEndpointPower();
//        endpointPower.setQoS(MqttQoS.AT_MOST_ONCE);
//        MqttEndpointPower endpointPower2 = new MqttEndpointPower();
//        endpointPower2.setQoS(MqttQoS.EXACTLY_ONCE);
//        MqttEndpointPower endpointPower3 = new MqttEndpointPower();
//        endpointPower3.setQoS(MqttQoS.AT_LEAST_ONCE);
//        RankTopic topics = new RankTopic("$");
//
//        String topic = "my_topic/sd/+/#";
//        topics.subscribe(topic, endpointPower);
//        topics.subscribe(topic, endpointPower2);
//        topics.subscribe(topic, endpointPower3);
//        topics.subscribe(topic, endpointPower);
//        topics.subscribe(topic, endpointPower);
//        topics.subscribe(topic, endpointPower);
//        System.out.println(topics);
////        topics.unSubscribe(topic, endpointPower);
////        System.out.println(topics);
//        String findTopic = "my_topic/sd/s";
//        LinkedList<MqttEndpointPower> list = topics.getSubscribeEndpointPowerLis(findTopic, new LinkedList<>());
//
//        System.out.println(list.size());


//        String topic = "fsa/dsad/dsa/dsa/dsa////";
//        while (true) {
//            int splitPosition = topic.indexOf("/");
//            String thisTopic = topic.substring(0, splitPosition);
//            topic = topic.substring(splitPosition + 1);
//            System.out.println("111111     " + thisTopic);
//            System.out.println(topic);
//            if (!topic.contains("/")) break;
//        }


//        LinkedList<RankTopics> a = new LinkedList<>();
//        a.add(new RankTopics("1"));
//        a.add(new RankTopics("2"));
//        a.add(new RankTopics("3"));
//        a.add(new RankTopics("4"));
//        a.add(new RankTopics("5"));
//        a.add(new RankTopics("6"));
//        a.add(new RankTopics("7"));
//        a.add(new RankTopics("8"));
//
//        System.out.println(a.get(0));
//        RankTopics rankTopics = a.get(0);
//        rankTopics.setTopic("sdfasgfsah");
//        System.out.println(a.get(0));
//
//        RankTopics rankTopics1 = a.get(0);
//        rankTopics1.setTopic("111111");
//        System.out.println(a.get(0));
//
//        a.add(new RankTopics("10"));
//        System.out.println(a.get(a.size() - 1));
//        a.remove(new RankTopics("10"));
//        System.out.println(a.get(a.size() - 1));


//        System.out.println(Arrays.toString(Arrays.stream("#".split("/",-1)).toArray()));
//        System.out.println(Arrays.toString(Arrays.stream("/+/".split("/",-1)).toArray()));
//        System.out.println(Arrays.toString(Arrays.stream("/+".split("/",-1)).toArray()));
//        System.out.println(Arrays.toString(Arrays.stream("+/+".split("/",-1)).toArray()));
//        System.out.println(Arrays.toString(Arrays.stream("+/////////////////////".split("/",-1)).toArray()));
//        System.out.println(Arrays.toString(Arrays.stream("/#".split("/",-1)).toArray()));
//        System.out.println(Arrays.toString(Arrays.stream("+/#".split("/",-1)).toArray()));
//        System.out.println(Arrays.toString(Arrays.stream("#/#".split("/",-1)).toArray()));
//        System.out.println(Arrays.toString(Arrays.stream("sport+”".split("/",-1)).toArray()));
//        System.out.println(Arrays.toString(Arrays.stream("sport/tennis#".split("/",-1)).toArray()));
//        System.out.println(Arrays.toString(Arrays.stream("sport/tennis/#/ranking".split("/",-1)).toArray()));
//        System.out.println(Arrays.toString(Arrays.stream("#/".split("/",-1)).toArray()));
//        System.out.println(Arrays.toString(Arrays.stream("#s".split("/",-1)).toArray()));
//        System.out.println(Arrays.toString(Arrays.stream("/##".split("/",-1)).toArray()));
    }


//    public static void main(String[] args) {
//        System.out.println(topicsValidate(new Topic("#")));
//        System.out.println(topicsValidate(new Topic("/+/")));
//        System.out.println(topicsValidate(new Topic("/+")));
//        System.out.println(topicsValidate(new Topic("+/+")));
//        System.out.println(topicsValidate(new Topic("+/")));
//        System.out.println(topicsValidate(new Topic("/#")));
//        System.out.println(topicsValidate(new Topic("+/#")));
//        System.out.println("------------------------------------------------");
//        System.out.println(topicsValidate(new Topic("#/#")));
//        System.out.println(topicsValidate(new Topic("sport+”")));
//        System.out.println(topicsValidate(new Topic("sport/tennis#")));
//        System.out.println(topicsValidate(new Topic("sport/tennis/#/ranking")));
//        System.out.println(topicsValidate(new Topic("#/")));
//        System.out.println(topicsValidate(new Topic("#s")));
//        System.out.println(topicsValidate(new Topic("/##")));

//    }

//
//    public static boolean topicsValidate(Topic topic) {
//        if ("#".equals(topic.getTopicName()) || "+".equals(topic.getTopicName())) return true;
//        String replace = topic.getTopicName();
//        if (replace.contains("+")) {
//            // 去除符合条件的+
//            replace = StringUtils.replace(replace, "/+/", "//");
//            if (replace.startsWith("+/")) {
//                // 去除最前端的   +/
//                replace = replace.substring(1);
//            }
//            if (replace.endsWith("/+")) {
//                // 去除最后端的    /+
//                replace = replace.substring(0, replace.length() - 1);
//            }
//            if (replace.contains("+")) {
//                //  剩下的 + 就是不符合条件的+
//                return false;
//            }
//        }
//        if (replace.contains("#")) {
//            // 如果有#  就必须在最后  且只能是/#  且只有一个
//            if (replace.endsWith("/#")) {
//                // 去除末尾
//                replace = replace.substring(0, replace.length() - 1);
//            }
//            return !replace.contains("#");
//        }
//        return true;
//    }
}
