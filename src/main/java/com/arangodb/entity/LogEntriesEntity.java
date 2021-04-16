/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.entity;

import java.util.List;

/**
 * @author Michele Rastelli
 * @see <a href= "https://www.arangodb.com/docs/stable/http/administration-and-monitoring.html#read-global-logs-from-the-server">API
 * Documentation</a>
 * @since ArangoDB 3.8
 */
public class LogEntriesEntity implements Entity {

    private Long total;
    private List<Message> messages;

    public Long getTotal() {
        return total;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public static class Message {
        Long id;
        String topic;
        String level;
        String date;
        String message;

        public Long getId() {
            return id;
        }

        public String getTopic() {
            return topic;
        }

        public String getLevel() {
            return level;
        }

        public String getDate() {
            return date;
        }

        public String getMessage() {
            return message;
        }
    }

}
