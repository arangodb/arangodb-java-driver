package at.orz.arangodb;

import at.orz.arangodb.entity.*;
import at.orz.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalAdminDriver extends BaseDriverInterface {
  AdminLogEntity getServerLog(
    Integer logLevel, Boolean logLevelUpTo,
    Integer start,
    Integer size, Integer offset,
    Boolean sortAsc,
    String text
  ) throws ArangoException;

  StatisticsEntity getStatistics() throws ArangoException;

  StatisticsDescriptionEntity getStatisticsDescription() throws ArangoException;

  ArangoVersion getVersion() throws ArangoException;

  ArangoUnixTime getTime() throws ArangoException;

  DefaultEntity reloadRouting() throws ArangoException;

  DefaultEntity executeScript(String database, String jsCode) throws ArangoException;
}
