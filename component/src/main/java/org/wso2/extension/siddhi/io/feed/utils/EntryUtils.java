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

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility static methods for the entry operations
 */
public class EntryUtils {
    // convert entry object to map object
    public static Map<String, String> entryToMap(Entry entry) {
        Map<String, String> map = new HashMap<>();

        map.put(Constants.ID, entry.getId().toString());
        map.put(Constants.TITLE, entry.getTitle());
        if (entry.getLinks() != null) {
            ArrayList<String> list = new ArrayList<>();
            for (Link l:entry.getLinks()) {
                list.add(l.getHref().toString());
            }
            map.put(Constants.LINK, String.join(", ", list));
        }
        if (entry.getUpdated() != null) {
            map.put(Constants.UPDATED, entry.getUpdated().toString());
        }
        if (entry.getAuthor() != null) {
            map.put(Constants.AUTHOR, entry.getAuthor().toString());
        }
        if (entry.getPublished() != null) {
            map.put(Constants.PUBLISHED, entry.getPublished().toString());
        }
        if (entry.getSummary() != null) {
            map.put(Constants.SUMMARY, entry.getSummary());
        }
        return map;
    }

    // put data to entry from map object
    public static Entry createEntry(HashMap<String, String> map, Entry entry) {
        for (String key: map.keySet()) {
            switch (key) {
                case Constants.ID : entry.setId(map.get(Constants.ID)); break;
                case Constants.TITLE : entry.setTitle(map.get(Constants.TITLE)); break;
                case Constants.CONTENT : entry.setContent(map.get(Constants.CONTENT)); break;
                case Constants.LINK : entry.addLink(map.get(Constants.LINK)); break;
                case Constants.AUTHOR : entry.addAuthor(map.get(Constants.AUTHOR)); break;
                case Constants.SUMMARY: entry.setSummary(map.get(Constants.SUMMARY)); break;
                default: break;
            }
        }
        entry.setUpdated(new Date());
        return entry;
    }
}
