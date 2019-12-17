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

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Content;
import org.apache.abdera.model.Person;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.apache.abdera.protocol.server.impl.AbstractEntityCollectionAdapter;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * News Adapter class for JettyServer
 */
public class NewsAdapter extends AbstractEntityCollectionAdapter<News> {
    private static final String ID_PREFIX = "wso2:news:db:";

    private AtomicInteger nextId = new AtomicInteger(1000);
    private Map<Integer, News> newsmap = new HashMap<Integer, News>();
    private Factory factory = new Abdera().getFactory();

    public String getId(RequestContext request) {
        return "tag:example.org,2019:feed";
    }

    public ResponseContext getCategories(RequestContext request) {
        return null;
    }

    @Override
    public News postEntry(String title,
                              IRI id,
                              String summary,
                              Date updated,
                              List<Person> authors,
                              Content content,
                              RequestContext request) throws ResponseContextException {
        News news = contentToNews(content);
        newsmap.put(news.getId(), news);
        return news;
    }

    private News contentToNews(Content content) {
        News news = new News();
        return contentToNews(content, news);
    }

    private News contentToNews(Content content, News news) {
        news.setName(content.getWrappedValue());
        news.setId(nextId.incrementAndGet());
        return news;
    }

    public void deleteEntry(String resourceName, RequestContext request) throws ResponseContextException {
        String id = resourceName.replace("-Content", "");
        newsmap.remove(Integer.parseInt(id));
    }

    public String getAuthor(RequestContext request) {
        return "WSO2";
    }

    @Override
    public List<Person> getAuthors(News entry, RequestContext request) throws ResponseContextException {
        Person author = request.getAbdera().getFactory().newAuthor();
        author.setName("WSO2");
        return Arrays.asList(author);
    }

    public Object getContent(News entry, RequestContext request) {
        Content content = factory.newContent();
        return entry.getName();
    }

    public Iterable<News> getEntries(RequestContext request) {
        return newsmap.values();
    }

    public News getEntry(String resourceName, RequestContext request) throws ResponseContextException {
        Integer id = getIdFromResourceName(resourceName);
        return newsmap.get(id);
    }

    private Integer getIdFromResourceName(String resourceName) throws ResponseContextException {
        int idx = resourceName.indexOf("-");
        if (idx == -1) {
            throw new ResponseContextException(404);
        }
        Integer id = new Integer(resourceName.substring(0, idx));
        return id;
    }

    public News getEntryFromId(String id, RequestContext request) {
        return newsmap.get(new Integer(id));
    }

    public String getId(News entry) {
        return ID_PREFIX + entry.getId();
    }

    public String getName(News entry) {
        return entry.getId() + "-" + entry.getName().replaceAll(" ", "_");
    }

    public String getTitle(RequestContext request) {
        return "WSO2 News Database";
    }

    public String getTitle(News entry) {
        return entry.getName();
    }

    public Date getUpdated(News entry) {
        return new Date();
    }

    @Override
    public void putEntry(News entry,
                         String title,
                         Date updated,
                         List<Person> authors,
                         String summary,
                         Content content,
                         RequestContext request) throws ResponseContextException {
        News news = newsmap.get(entry.getId());
        news.setName(title);
    }

}
