/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.streams.examples.schema;

import org.apache.rocketmq.streams.client.StreamBuilder;
import org.apache.rocketmq.streams.client.source.DataStreamSource;
import org.apache.rocketmq.streams.client.strategy.WindowStrategy;
import org.apache.rocketmq.streams.client.transform.window.Time;
import org.apache.rocketmq.streams.client.transform.window.TumblingWindow;
import org.apache.rocketmq.streams.examples.send.ProducerFromFile;
import org.apache.rocketmq.streams.examples.source.Data;
import org.apache.rocketmq.streams.schema.SchemaConfig;
import org.apache.rocketmq.streams.schema.SchemaType;

import static org.apache.rocketmq.streams.examples.aggregate.Constant.NAMESRV_ADDRESS;

public class SchemaRocketmqWindowExample {

    private static String topicName = "schema-windowTopic";
    private static String groupName = "windowTopicGroup";

    public static void main(String[] args) {
        ProducerFromFile.produce("data-2.txt",NAMESRV_ADDRESS, topicName, true);

        try {
            Thread.sleep(1000 * 3);
        } catch (InterruptedException e) {
        }
        System.out.println("begin streams code.");

        DataStreamSource source = StreamBuilder.dataStream("namespace", "pipeline");
        source.fromRocketmq(
            topicName,
            groupName,
            NAMESRV_ADDRESS,
            new SchemaConfig(SchemaType.JSON, Data.class))
            .filter(data -> ((Data)data).getInFlow() > 2)
            .window(TumblingWindow.of(Time.seconds(5)))
            .groupBy("projectName","logStore")
            .sum("outFlow", "outFlow")
            .sum("inFlow", "inFlow")
            .count("total")
            .waterMark(2)
            .setLocalStorageOnly(false)
            .toDataStream()
            .toPrint(1)
            .with(WindowStrategy.highPerformance())
            .start();
    }
}
