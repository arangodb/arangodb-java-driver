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

package at.orz.arangodb;

/**
 * Test POJO.
 * Station Information in Tokyo, Japan.
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class Station {
	
	private int companyCode;
	private String company;
	private int lineCode;
	private String line;
	private int stationCode;
	private String station;
	private String address;
	private double lon;
	private double lat;
	
	public Station() {
		
	}
	
	public Station(String[] items) {
		this.companyCode = Integer.parseInt(items[0]);
		this.company = items[1];
		this.lineCode = Integer.parseInt(items[2]);
		this.line = items[3];
		this.stationCode = Integer.parseInt(items[4]);
		this.station = items[5];
		this.address = items[6];
		this.lon = Double.parseDouble(items[7]);
		this.lat = Double.parseDouble(items[8]);
	}
	
	public int getCompanyCode() {
		return companyCode;
	}
	public String getCompany() {
		return company;
	}
	public int getLineCode() {
		return lineCode;
	}
	public String getLine() {
		return line;
	}
	public int getStationCode() {
		return stationCode;
	}
	public String getStation() {
		return station;
	}
	public String getAddress() {
		return address;
	}
	public double getLon() {
		return lon;
	}
	public double getLat() {
		return lat;
	}
	public void setCompanyCode(int companyCode) {
		this.companyCode = companyCode;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public void setLineCode(int lineCode) {
		this.lineCode = lineCode;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public void setStationCode(int stationCode) {
		this.stationCode = stationCode;
	}
	public void setStation(String station) {
		this.station = station;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	
}
