package com.arangodb;

import com.arangodb.entity.AdminLogEntity;
import com.arangodb.entity.ArangoUnixTime;
import com.arangodb.entity.ArangoVersion;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.StatisticsDescriptionEntity;
import com.arangodb.entity.StatisticsEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalAdminDriver extends BaseDriverInterface {
	AdminLogEntity getServerLog(
		Integer logLevel,
		Boolean logLevelUpTo,
		Integer start,
		Integer size,
		Integer offset,
		Boolean sortAsc,
		String text) throws ArangoException;

	/**
	 * Get the ArangoDB database statistics
	 * 
	 * @return the database statistics
	 * @throws ArangoException
	 */
	StatisticsEntity getStatistics() throws ArangoException;

	StatisticsDescriptionEntity getStatisticsDescription() throws ArangoException;

	ArangoVersion getVersion() throws ArangoException;

	ArangoUnixTime getTime() throws ArangoException;

	DefaultEntity reloadRouting() throws ArangoException;

	DefaultEntity executeScript(String database, String jsCode) throws ArangoException;
}
