/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.io.feed.sink;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.feed.source.TestCaseOfFeedSource;
import org.wso2.extension.siddhi.io.feed.utils.JettyServer;
import org.wso2.extension.siddhi.io.feed.utils.PortAllocator;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;


public class TestCaseOfFeedSink {
    private Logger log = Logger.getLogger(TestCaseOfFeedSource.class.getName());
    private static JettyServer server;
    private static AbderaClient client = new AbderaClient();
    private static String base;

    @BeforeClass
    public void startServer() throws Exception {
        int port = PortAllocator.allocatePort();
        server = new JettyServer(port);
        server.start();
        base = "http://localhost:" + port + "/news";
    }

    @AfterClass
    public void stopServer() throws Exception {
        server.stop();
    }

    @Test
    public void basicSink() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           FEED Create for Sink Test                                 ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +
                "@sink(type='feed', \n" +
                "url = '" + base + "', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false')) \n" +
                " define stream outputStream(id string, title string, content string," +
                " link string, author string, name string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("outputStream");
        executionPlanRuntime.start();

        inputHandler.send(new Object[]{base + "/feed/entries/1", "Title1", "Content", "sample.com", "David", "A"});
        inputHandler.send(new Object[]{base + "/feed/entries/2", "Title2", "Content", "example.com", "Renaldo", "B"});
        inputHandler.send(new Object[]{base + "/feed/entries/3", "Title3", "Content", "example7.com", "Harry", "C"});
        inputHandler.send(new Object[]{base + "/feed/entries/2", "Title4", "Content", "example.com", "Crist", "D"});

        siddhiManager.shutdown();

        ClientResponse resp = client.get(base);
        Document<Feed> feedDoc = resp.getDocument();
        Feed feed = feedDoc.getRoot();
        Entry entry = feed.getEntries().get(0);
        Assert.assertEquals(entry.getContent(), "Content");
    }

    @Test(dependsOnMethods = "basicSink")
    public void sinkForUpdate() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                             FEED Sink for UPDATE Test                               ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        ClientResponse resp = client.get(base);
        Document<Feed> feedDoc = resp.getDocument();
        Feed feed = feedDoc.getRoot();
        Entry entry = feed.getEntries().get(1);
        String editLink = entry.getEditLinkResolvedHref().toString();
        resp.release();

        String siddhiApp = "@App:name('test') \n" +
                "@sink(type='feed', \n" +
                "url = '" + editLink + "', \n" +
                "http.response.code = '204', \n" +
                "atom.func = 'update', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false')) \n" +
                " define stream outputStream(content string, title string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("outputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"XXXXXXX", "AAAAAA"});
        siddhiManager.shutdown();
        resp = client.get(base);
        feedDoc = resp.getDocument();
        feed = feedDoc.getRoot();
        Entry entry1 = feed.getEntries().get(1);
        Assert.assertEquals(entry1.getTitle(), "AAAAAA");
    }

    @Test(dependsOnMethods = "sinkForUpdate")
    public void sinkForDelete() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           FEED Sink for DELETE Test                                 ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@sink(type='feed', \n" +
                "url = '" + base + "', \n" +
                "http.response.code = '204', \n" +
                "atom.func = 'delete', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false')) \n" +
                " define stream outputStream(id string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("outputStream");
        executionPlanRuntime.start();

        ClientResponse resp = client.get(base);
        Document<Feed> feedDoc = resp.getDocument();
        Feed feed = feedDoc.getRoot();
        Entry entry = feed.getEntries().get(0);

        String edit = entry.getEditLinkResolvedHref().toString();
        resp.release();

        inputHandler.send(new Object[]{edit});
        siddhiManager.shutdown();

        ClientResponse resp2 = client.get(base);
        Document<Feed> feedDoc2 = resp2.getDocument();
        Feed feed2 = feedDoc2.getRoot();
        Entry entry2 = feed2.getEntries().get(0);

        Assert.assertNotEquals(entry2.getId(), entry.getId());
        resp.release();
    }

    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void sinkForValidation() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           FEED sink for Url Malformed Exception Test                ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@Sink(type='feed', \n" +
                "feed.type = 'Atomm', \n" +
                "url = 'localhost.', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false')) \n" +
                " define stream outputStream(id string, published string, content string, title string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });
    }

    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void sinkValidationTest() {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                  FEED Sink for atom function validation Test                        ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@sink(type='feed', \n" +
                "url = '" + base + "', \n" +
                "http.response.code = '204', \n" +
                "atom.func = 'error value', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false')) \n" +
                " define stream outputStream(content string, title string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });
    }
}
