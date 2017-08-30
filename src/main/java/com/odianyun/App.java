package com.odianyun;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.kafka.*;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;

import java.util.Arrays;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        //zookeeper的服务器地址
        String zks = "172.16.0.202:2181,172.16.0.203:2181,172.16.0.204:2181";
        String zk_list = "172.16.0.202,172.16.0.203,172.16.0.204";
        //消息的topic
        String topic = "mytopic";
        //strom在zookeeper上的根
        String zkRoot = "/live";
        String id = "streaming";
        BrokerHosts brokerHosts = new ZkHosts(zks);
        SpoutConfig spoutConfig = new SpoutConfig(brokerHosts, topic, zkRoot, id);
        spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        spoutConfig.socketTimeoutMs = 10000;
        spoutConfig.fetchMaxWait = 10000;
        spoutConfig.bufferSizeBytes = 1024 * 1024;
        spoutConfig.maxOffsetBehind = Long.MAX_VALUE;
        // 设置从头开始读取
        spoutConfig.startOffsetTime = kafka.api.OffsetRequest.EarliestTime();
        spoutConfig.useStartOffsetTimeIfOffsetOutOfRange = true;
        spoutConfig.metricsTimeBucketSizeInSecs = 60;

        spoutConfig.retryLimit = 3;
//        spoutConfig.zkServers = Arrays.asList(zk_list.split(","));
//        spoutConfig.zkPort = 2181;
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("kafka-reader", new KafkaSpout(spoutConfig),3);
        builder.setBolt("behavior", new BehaviorBolt(),3).shuffleGrouping("kafka-reader");
        Config conf = new Config();
        conf.setDebug(false);
        //设置任务线程数
        conf.setMaxTaskParallelism(1);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("behaviorTest", conf, builder.createTopology());
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cluster.shutdown();
    }
}
