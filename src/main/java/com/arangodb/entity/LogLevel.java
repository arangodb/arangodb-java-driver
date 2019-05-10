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

/**
 * @author Mark Vollmary
 *
 */
public enum LogLevel {

	FATAL(0), ERROR(1), WARNING(2), INFO(3), DEBUG(4);

	private final int level;

	LogLevel(final int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public static LogLevel fromLevel(final int level) {
		for (final LogLevel logLevel : LogLevel.values()) {
			if (logLevel.level == level) {
				return logLevel;
			}
		}
		return null;
	}

}
