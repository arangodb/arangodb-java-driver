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
import java.math.BigDecimal;
import java.util.List;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class StatisticsDescriptionEntity extends BaseEntity {

	List<Group> groups;
	List<Figure> figures;
	
	public List<Group> getGroups() {
		return groups;
	}

	public List<Figure> getFigures() {
		return figures;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public void setFigures(List<Figure> figures) {
		this.figures = figures;
	}

	
	public static class Group implements Serializable {
		String group;
		String name;
		String description;
		public String getGroup() {
			return group;
		}
		public String getName() {
			return name;
		}
		public String getDescription() {
			return description;
		}
		public void setGroup(String group) {
			this.group = group;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		
	}
	
	public static class Figure implements Serializable {
		String group;
		String identifier;
		String name;
		String description;
		String type;
		String units;
		BigDecimal[] cuts;
		public String getGroup() {
			return group;
		}
		public String getIdentifier() {
			return identifier;
		}
		public String getName() {
			return name;
		}
		public String getDescription() {
			return description;
		}
		public String getType() {
			return type;
		}
		public String getUnits() {
			return units;
		}
		public BigDecimal[] getCuts() {
			return cuts;
		}
		public void setGroup(String group) {
			this.group = group;
		}
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public void setType(String type) {
			this.type = type;
		}
		public void setUnits(String units) {
			this.units = units;
		}
		public void setCuts(BigDecimal[] cuts) {
			this.cuts = cuts;
		}
	}
	
}
