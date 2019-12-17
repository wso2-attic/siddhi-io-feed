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

package org.wso2.extension.siddhi.io.feed.utils;

import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.impl.DefaultProvider;
import org.apache.abdera.protocol.server.impl.SimpleWorkspaceInfo;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * for create mock http sever
 */
public class JettyServer {
    private final int port;
    private Server server;
    private DefaultProvider customerProvider;

    public JettyServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        server = new Server(port);
        setupAbdera("/");
        Context context = new Context(server, "/", Context.SESSIONS);
        context.addServlet(new ServletHolder(new AbderaServlet() {
            @Override
            protected Provider createProvider() {
                customerProvider.init(getAbdera(), null);
                return customerProvider;
            }
        }), "/");
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    private void setupAbdera(String base) throws Exception {
        customerProvider = new DefaultProvider(base);

        NewsAdapter newsAdapter = new NewsAdapter();
        newsAdapter.setHref("news");

        SimpleWorkspaceInfo wi = new SimpleWorkspaceInfo();
        wi.setTitle("Customer Workspace");
        wi.addCollection(newsAdapter);

        customerProvider.addWorkspace(wi);
    }

}
