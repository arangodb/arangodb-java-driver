package com.arangodb;

import com.arangodb.entity.CursorEntity;

import java.util.List;

public interface BaseArangoCursor<T> {
    String getId();

    Long getCount();

    Boolean isCached();

    Boolean hasMore();

    List<T> getResult();

    Boolean isPotentialDirtyRead();

    String getNextBatchId();

    CursorEntity.Extra getExtra();
}
