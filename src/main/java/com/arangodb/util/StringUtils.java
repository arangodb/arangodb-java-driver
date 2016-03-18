/*
 * Copyright (C) 2012 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class StringUtils {

	private static Logger logger = LoggerFactory.getLogger(StringUtils.class);

	private StringUtils() {
		// this is a helper class
	}

	public static String encodeUrl(String text) {
		if (text != null) {
			try {
				return URLEncoder.encode(text, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("could not encode an URL", e);
			}
		}
		return null;
	}

	public static boolean isNotEmpty(String str) {
		return str != null && str.length() > 0;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static boolean isCurlyBracketStart(String text) {

		if (text == null || "".equals(text)) {
			return false;
		}

		int i = 0;
		while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
			i++;
		}
		return i < text.length() && text.charAt(i) == '{';

	}

	public static String join(String... params) {
		if (params == null) {
			return null;
		}
		return join(false, Arrays.asList(params));
	}

	public static String join(boolean endSlash, Collection<String> paths) {
		if (CollectionUtils.isNotEmpty(paths)) {
			return joinCollection(endSlash, paths);
		}
		return null;
	}

	private static String joinCollection(boolean endSlash, Collection<String> paths) {
		boolean prevLastSlash = false;
		StringBuilder buffer = new StringBuilder();
		for (String param : paths) {
			if (param == null)
				continue;
			if (!prevLastSlash && !param.startsWith("/")) {
				buffer.append('/');
			}
			if (prevLastSlash && param.startsWith("/")) {
				buffer.append(param.substring(1));
			} else {
				buffer.append(param);
			}
			prevLastSlash = param.endsWith("/");
		}
		if (endSlash && !prevLastSlash) {
			buffer.append('/');
		}
		return buffer.toString();
	}

}
