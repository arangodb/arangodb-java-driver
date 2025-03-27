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
import java.util.Objects;

/**
 * @author Michele Rastelli
 * @since ArangoDB 3.8
 */
public final class LogEntriesEntity {

    private Long total;
    private List<Message> messages;

    public Long getTotal() {
        return total;
    }

    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LogEntriesEntity)) return false;
        LogEntriesEntity that = (LogEntriesEntity) o;
        return Objects.equals(total, that.total) && Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, messages);
    }

    public static final class Message {
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Message)) return false;
            Message message1 = (Message) o;
            return Objects.equals(id, message1.id) && Objects.equals(topic, message1.topic) && Objects.equals(level, message1.level) && Objects.equals(date, message1.date) && Objects.equals(message, message1.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, topic, level, date, message);
        }
    }

}
