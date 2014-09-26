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

package at.orz.arangodb.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class StatisticsEntity extends BaseEntity {

	System system;
	Client client;
	Server server;

	public System getSystem() {
		return system;
	}

	public Client getClient() {
		return client;
	}

	public Server getServer() {
		return server;
	}

	public void setSystem(System system) {
		this.system = system;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public void setServer(Server server) {
		this.server = server;
	}
	
	
	public static class FigureValue implements Serializable {
		double sum;
		long count;
		long[] counts;
		public double getSum() {
			return sum;
		}
		public long getCount() {
			return count;
		}
		public long[] getCounts() {
			return counts;
		}
		public void setSum(double sum) {
			this.sum = sum;
		}
		public void setCount(long count) {
			this.count = count;
		}
		public void setCounts(long[] counts) {
			this.counts = counts;
		}
		
	}
	
	public static class Client implements Serializable {
		int httpConnections;
		Map<String, FigureValue> figures;
		public int getHttpConnections() {
			return httpConnections;
		}
		public Map<String, FigureValue> getFigures() {
			return figures;
		}
		public void setHttpConnections(int httpConnections) {
			this.httpConnections = httpConnections;
		}
		public void setFigures(Map<String, FigureValue> figures) {
			this.figures = figures;
		}
	}
	
	public static class Server implements Serializable {
		double uptime;

		public double getUptime() {
			return uptime;
		}

		public void setUptime(double uptime) {
			this.uptime = uptime;
		}
		
	}
	
	public static class System implements Serializable {
		
		long minorPageFaults;
		long majorPageFaults;
		double userTime;
		double systemTime;
		int numberOfThreads;
		long residentSize;
		long virtualSize;
		
		public long getMinorPageFaults() {
			return minorPageFaults;
		}
		public long getMajorPageFaults() {
			return majorPageFaults;
		}
		public double getUserTime() {
			return userTime;
		}
		public double getSystemTime() {
			return systemTime;
		}
		public int getNumberOfThreads() {
			return numberOfThreads;
		}
		public long getResidentSize() {
			return residentSize;
		}
		public long getVirtualSize() {
			return virtualSize;
		}
		public void setMinorPageFaults(long minorPageFaults) {
			this.minorPageFaults = minorPageFaults;
		}
		public void setMajorPageFaults(long majorPageFaults) {
			this.majorPageFaults = majorPageFaults;
		}
		public void setUserTime(double userTime) {
			this.userTime = userTime;
		}
		public void setSystemTime(double systemTime) {
			this.systemTime = systemTime;
		}
		public void setNumberOfThreads(int numberOfThreads) {
			this.numberOfThreads = numberOfThreads;
		}
		public void setResidentSize(long residentSize) {
			this.residentSize = residentSize;
		}
		public void setVirtualSize(long virtualSize) {
			this.virtualSize = virtualSize;
		}
		
	}

	
}
