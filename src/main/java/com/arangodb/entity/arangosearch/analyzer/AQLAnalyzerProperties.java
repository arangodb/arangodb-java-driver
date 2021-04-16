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


package com.arangodb.entity.arangosearch.analyzer;


import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public class AQLAnalyzerProperties {

    private String queryString;
    private Boolean collapsePositions;
    private Boolean keepNull;
    private Integer batchSize;
    private Long memoryLimit;

    private ReturnType returnType;

    public enum ReturnType {
        string, number, bool
    }

    /**
     * @return AQL query to be executed
     */
    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    /**
     * @return <ul>
     * <li>
     * true: set the position to 0 for all members of the query result array
     * </li>
     * <li>
     * false (default): set the position corresponding to the index of the result array member
     * </li>
     * </ul>
     */
    public Boolean getCollapsePositions() {
        return collapsePositions;
    }

    public void setCollapsePositions(Boolean collapsePositions) {
        this.collapsePositions = collapsePositions;
    }

    /**
     * @return <ul>
     * <li>
     * true (default): treat null like an empty string
     * </li>
     * <li>
     * false: discard nulls from View index. Can be used for index filtering (i.e. make your query return null for unwanted data). Note that empty results are always discarded.
     * </li>
     * </ul>
     */
    public Boolean getKeepNull() {
        return keepNull;
    }

    public void setKeepNull(Boolean keepNull) {
        this.keepNull = keepNull;
    }

    /**
     * @return number between 1 and 1000 (default = 1) that determines the batch size for reading data from the query.
     * In general, a single token is expected to be returned. However, if the query is expected to return many results,
     * then increasing batchSize trades memory for performance.
     */
    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * @return memory limit for query execution in bytes. (default is 1048576 = 1Mb) Maximum is 33554432U (32Mb)
     */
    public Long getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(Long memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    /**
     * @return data type of the returned tokens. If the indicated type does not match the actual type then an implicit
     * type conversion is applied.
     */
    public ReturnType getReturnType() {
        return returnType;
    }

    public void setReturnType(ReturnType returnType) {
        this.returnType = returnType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AQLAnalyzerProperties that = (AQLAnalyzerProperties) o;
        return Objects.equals(queryString, that.queryString) && Objects.equals(collapsePositions, that.collapsePositions) && Objects.equals(keepNull, that.keepNull) && Objects.equals(batchSize, that.batchSize) && Objects.equals(memoryLimit, that.memoryLimit) && returnType == that.returnType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryString, collapsePositions, keepNull, batchSize, memoryLimit, returnType);
    }

}
