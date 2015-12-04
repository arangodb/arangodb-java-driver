/*
 * Copyright (C) 2012,2013 tamtam180
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.arangodb.Station;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class TestUtils {

	public static final String VERSION_2_7 = "2.7";

	public static final String VERSION_2_8 = "2.8";

	public static List<Station> readStations() throws IOException {

		ArrayList<Station> stations = new ArrayList<Station>(1000);
		BufferedReader br = new BufferedReader(
				new InputStreamReader(TestUtils.class.getResourceAsStream("/test-data/jp-tokyo-station.tsv"), "utf-8"));
		String line = null;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0) {
				continue;
			}
			Station station = new Station(line.split("  ", -1));
			stations.add(station);
		}
		br.close();

		return stations;

	}

	public static int compareVersion(String version1, String version2) {
		String[] v1s = version1.split("\\.");
		String[] v2s = version2.split("\\.");

		int minLength = Math.min(v1s.length, v2s.length);
		int i = 0;
		for (; i < minLength; i++) {
			int i1 = getIntegerValueOfString(v1s[i]);
			int i2 = getIntegerValueOfString(v2s[i]);
			if (i1 > i2)
				return 1;
			else if (i1 < i2)
				return -1;
		}
		int sum1 = 0;
		for (int j = 0; j < v1s.length; j++) {
			sum1 += getIntegerValueOfString(v1s[j]);
		}
		int sum2 = 0;
		for (int j = 0; j < v2s.length; j++) {
			sum2 += getIntegerValueOfString(v2s[j]);
		}
		if (sum1 == sum2)
			return 0;
		return v1s.length > v2s.length ? 1 : -1;
	}

	private static int getIntegerValueOfString(String str) {
		try {
			return Integer.valueOf(str);
		} catch (NumberFormatException e) {
			if (str.contains("-")) {
				str = str.substring(0, str.indexOf('-'));
				return Integer.valueOf(str);
			}
		}
		return 0;
	}

}
