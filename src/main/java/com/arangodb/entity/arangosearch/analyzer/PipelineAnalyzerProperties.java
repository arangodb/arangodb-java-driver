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

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Michele Rastelli
 */
public class PipelineAnalyzerProperties {
    private List<SearchAnalyzer> pipeline = new LinkedList<>();

    /**
     * Appends the specified analyzer to the end of the pipeline.
     * <p/>
     * <b>LIMITATIONS</b>: Analyzers of types {@link GeoPointAnalyzer} and {@link GeoJSONAnalyzer} cannot be used in
     * pipelines and will make the creation fail.
     * <p/>
     *
     * @param analyzer analyzer to be appended
     * @return this
     */
    public PipelineAnalyzerProperties addAnalyzer(final SearchAnalyzer analyzer) {
        pipeline.add(analyzer);
        return this;
    }

    /**
     * @return an array of Analyzer with type and properties attributes
     */
    public List<SearchAnalyzer> getPipeline() {
        return pipeline;
    }

    public void setPipeline(List<SearchAnalyzer> pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PipelineAnalyzerProperties that = (PipelineAnalyzerProperties) o;
        return Objects.equals(pipeline, that.pipeline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pipeline);
    }

}
