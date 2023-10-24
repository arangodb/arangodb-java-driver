package com.arangodb;

import com.arangodb.entity.CursorEntity;

import java.util.List;

public interface BaseArangoCursor<T> {
    String getId();

    Integer getCount();

    Boolean isCached();

    Boolean hasMore();

    List<T> getResult();

    Boolean isPotentialDirtyRead();

    String getNextBatchId();

    CursorEntity.Extras getExtra();
}
