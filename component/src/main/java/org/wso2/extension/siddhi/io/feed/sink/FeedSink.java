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

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.wso2.extension.siddhi.io.feed.sink.exceptions.FeedErrorResponseException;
import org.wso2.extension.siddhi.io.feed.utils.Constants;
import org.wso2.extension.siddhi.io.feed.utils.EntryUtils;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The sink implementation of feed.
 */
@Extension(
        name = "feed",
        namespace = "sink",
        description = " The feed sink allows to publish atom feed entries to atom implemented http servers. " +
                "The stream variables should be the standard element name of the entry of atom document. 'id'," +
                " 'title', 'link', 'updated', 'author', 'published', 'summery' are the supported elements.",
        parameters = {
                @Parameter(name = Constants.URL,
                        description = "The feed end point url",
                        type = DataType.STRING),
                @Parameter(name = Constants.ATOM_FUNC,
                        description = "Atom function of the request. " +
                                "Acceptance parameters are 'create', 'delete', 'update'",
                        type = DataType.STRING),
                @Parameter(name = Constants.USERNAME,
                        description = "User name of the basic authentication",
                        optional = true,
                        defaultValue = Constants.DEFAULT_USERNAME,
                        type = DataType.STRING),
                @Parameter(name = Constants.PASSWORD,
                        description = "Password of the basic authentication",
                        optional = true,
                        defaultValue = Constants.DEFAULT_PASSWORD,
                        type = DataType.INT),
                @Parameter(name = Constants.HTTP_RESPONSE_CODE,
                        description = "Http response code",
                        optional = true,
                        defaultValue = Constants.HTTP_CREATED,
                        type = DataType.INT),
                @Parameter(name = Constants.TIME_OUT,
                        description = "time out of the atom http response, in milliseconds",
                        optional = true,
                        defaultValue = Constants.DEFAULT_TIME_OUT,
                        type = DataType.INT)
        },
        examples = {
                @Example(
                        syntax = "@App:name('test')n" +
                                "@sink(type='feed',\n" +
                                "url = 'localhost:8080/news',\n" +
                                "http.response.code = '202',\n" +
                                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false'))\n" +
                                " define stream outputStream(content string, title string);",
                        description = " This example shows how to create Atom entry on existing atom document. The " +
                                "variables of stream are the standard element of a atom entry "
                ),
                @Example(
                        syntax = "@App:name('test')n" +
                                "@sink(type='feed',\n" +
                                "url = 'localhost:8080/news',\n" +
                                "http.response.code = '204',\n" +
                                "atom.func = 'delete',\n" +
                                "@map(type = 'keyvalue'))\n" +
                                " define stream outputStream(id string);",
                        description = " This example shows how to delete Atom entry on existing atom document. The " +
                                "'id' variable is a standard element of a atom entry "
                ),
                @Example(
                        syntax = "@App:name('test')n" +
                                "@sink(type='feed',\n" +
                                "url = 'localhost:8080/news',\n" +
                                "http.response.code = '200',\n" +
                                "atom.func = 'update',\n" +
                                "@map(type = 'keyvalue'))\n" +
                                " define stream outputStream(id string, content string, title string);",
                        description = " This example shows how to update Atom entry on existing atom document. The " +
                                "variables of stream are the standard element of a atom entry "
                )
        }
)

public class FeedSink extends Sink {

    private URL url;
    private Abdera abdera;
    private AbderaClient abderaClient;
    private int httpResponse;
    private String atomFunc;
    private StreamDefinition streamDefinition;
    private String siddhiAppName;

    @Override
    protected void init(StreamDefinition streamDefinition, OptionHolder optionHolder, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        this.siddhiAppName = siddhiAppContext.getName();
        try {
            this.url = new URL(optionHolder.validateAndGetStaticValue(Constants.URL));
        } catch (MalformedURLException e) {
            throw new SiddhiAppValidationException("Url Syntax Error in siddhi app: " + siddhiAppName + ". stream name "
                    + streamDefinition.getId() + ". Given value "
                    + optionHolder.validateAndGetStaticValue(Constants.URL), e);
        }
        this.streamDefinition = streamDefinition;
        this.abdera = new Abdera();
        this.abderaClient = new AbderaClient(abdera);
        this.httpResponse = Integer.parseInt(optionHolder.validateAndGetStaticValue(Constants.HTTP_RESPONSE_CODE,
                Constants.HTTP_CREATED));
        this.atomFunc = validateAtom(optionHolder.validateAndGetStaticValue(Constants.ATOM_FUNC,
                Constants.FEED_CREATE));
        try {
            int timeout = Integer.parseInt(optionHolder.validateAndGetStaticValue(Constants.TIME_OUT,
                    Constants.DEFAULT_TIME_OUT));
            if (timeout > 0) {
                this.abderaClient.setConnectionTimeout(timeout);
            } else {
                throw new SiddhiAppValidationException("Error in siddhi app: " + siddhiAppName + " in stream "
                        + streamDefinition.getId()
                        + " validating timeout, Response timeout accept only positive integers. But found " +
                        optionHolder.validateAndGetStaticValue(Constants.TIME_OUT,
                                Constants.DEFAULT_TIME_OUT));
            }
        } catch (NumberFormatException e) {
            throw new SiddhiAppValidationException("Error in siddhi app: " + siddhiAppName + " in stream "
                    + streamDefinition.getId()
                    + " validating timeout, Response timeout accept only positive integers. But found " +
                    optionHolder.validateAndGetStaticValue(Constants.TIME_OUT,
                            Constants.DEFAULT_TIME_OUT));
        }
        String userName = optionHolder.validateAndGetStaticValue(Constants.USERNAME, Constants.DEFAULT_USERNAME);
        String password = optionHolder.validateAndGetStaticValue(Constants.PASSWORD, Constants.DEFAULT_PASSWORD);
        if (!optionHolder.validateAndGetStaticValue(Constants.USERNAME, Constants.DEFAULT_USERNAME)
                .equals(Constants.DEFAULT_USERNAME)
                || !optionHolder.validateAndGetStaticValue(Constants.PASSWORD, Constants.DEFAULT_PASSWORD)
                .equals(Constants.DEFAULT_PASSWORD)) {
            AbderaClient.registerTrustManager();
            try {
                abderaClient.addCredentials(String.valueOf(url), Constants.REALM, Constants.AUTH_SCHEME,
                        new UsernamePasswordCredentials(userName, password));
            } catch (URISyntaxException e) {
                /** URISyntaxException will not be thrown here as the URL is already validated */
            }
        }
    }

    @Override
    public Class[] getSupportedInputEventClasses() {
            return new Class[]{Map.class};
    }

    @Override
    public String[] getSupportedDynamicOptions() {
            return new String[0];
    }

    /** atom function validation */
    private String validateAtom(String atomFunc) {
        atomFunc = atomFunc.toLowerCase(Locale.ENGLISH);
        if (atomFunc.equals(Constants.FEED_UPDATE)
                || atomFunc.equals(Constants.FEED_DELETE) || atomFunc.equals(Constants.FEED_CREATE)) {
            return atomFunc;
        }
        throw new SiddhiAppValidationException("Atom function validation error  in siddhi app: " + siddhiAppName
                + " in stream " + streamDefinition.getId()
                + ". Acceptance parameters are 'create', 'delete', 'update'");
    }

    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions) throws ConnectionUnavailableException {
        HashMap<String, String> map = (HashMap) payload;
        ClientResponse resp = null;
        switch (atomFunc) {
            case Constants.FEED_CREATE: {
                Entry entry = EntryUtils.createEntry(map, abdera.newEntry());
                entry.setPublished(new Date());
                try {
                    resp = abderaClient.post(url.toString(), entry);
                } catch (RuntimeException exception) {
                    throw new FeedErrorResponseException("Connection timeout exception in siddhi app: " + siddhiAppName
                            + " in stram " + streamDefinition.getId()
                            + ". The host did not accept the connection within timeout of "
                            + abderaClient.getConnectionTimeout());
                }
                break;
            }
            case Constants.FEED_DELETE: {
                try {
                    resp = abderaClient.delete(map.get("id"));
                } catch (RuntimeException exception) {
                    throw new FeedErrorResponseException("Connection timeout exception in siddhi app: " + siddhiAppName
                            + " in stram " + streamDefinition.getId()
                            + ". The host did not accept the connection within timeout of "
                            + abderaClient.getConnectionTimeout());
                }
                break;
            }
            case Constants.FEED_UPDATE: {
                try {
                    resp = abderaClient.get(url.toString());
                    Document<Entry> doc = resp.getDocument();
                    Entry entry = doc.getRoot();
                    entry = EntryUtils.createEntry(map, entry);
                    resp = abderaClient.put(url.toString(), entry);
                }  catch (RuntimeException exception) {
                    throw new FeedErrorResponseException("Connection timeout exception in siddhi app: " + siddhiAppName
                            + " in stram " + streamDefinition.getId()
                            + ". The host did not accept the connection within timeout of "
                            + abderaClient.getConnectionTimeout());
                }
                break;
            }
            /** default and other cases are not possible due to validation */
        }

        if (resp != null) {
            if (resp.getStatus() != httpResponse) {
                throw new FeedErrorResponseException("Response status conflicts in siddhi app: " + siddhiAppName
                        + " in stream " + streamDefinition.getId()
                        + " response status code is : " + resp.getStatus() + "-" + resp.getStatusText());
            }
            resp.release();
        } else {
            throw new FeedErrorResponseException("Response is null in siddhi app " + siddhiAppName + " in " +
                    "stream " + streamDefinition.getId() + " from url " + url.toString());
        }
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void destroy() {
        abderaClient.clearCredentials();
    }

    @Override
    public Map<String, Object> currentState() {
            return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {
    }
}
