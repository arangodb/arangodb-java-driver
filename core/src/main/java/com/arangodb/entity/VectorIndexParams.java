package com.arangodb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * @author Michele Rastelli
 * @since ArangoDB 3.12
 */
public final class VectorIndexParams {
    private Integer defaultNProbe;
    private Integer dimension;
    private String factory;
    private Metric metric;
    @JsonProperty("nLists")
    private NLists nLists;
    private Integer numberOfDocsPerCentroid;
    private Integer trainingIterations;

    public Integer getDefaultNProbe() {
        return defaultNProbe;
    }

    /**
     * @param defaultNProbe How many neighboring centroids to consider for the search results by default. The larger
     *                      the number, the slower the search but the better the search results. The default is 1.
     *                      You should generally use a higher value here or per query via the nProbe option of the
     *                      vector similarity functions.
     * @return this
     */
    public VectorIndexParams defaultNProbe(Integer defaultNProbe) {
        this.defaultNProbe = defaultNProbe;
        return this;
    }

    public Integer getDimension() {
        return dimension;
    }

    /**
     * @param dimension The vector dimension. The attribute to index needs to have this many elements in the array
     *                  that stores the vector embedding.
     * @return this
     */
    public VectorIndexParams dimension(Integer dimension) {
        this.dimension = dimension;
        return this;
    }

    public String getFactory() {
        return factory;
    }

    /**
     * @param factory You can specify an index factory string that is forwarded to the underlying Faiss library,
     *                allowing you to combine different advanced options. Examples:
     *                <p>
     *                "IVF100_HNSW10,Flat"
     *                "IVF100,SQ4"
     *                "IVF10_HNSW5,Flat"
     *                "IVF100_HNSW5,PQ256x16"
     *                "IVF{},SQ4"
     *                <p>
     *                The base index must be an inverted file (IVF) to work with ArangoDB. If you don’t specify an
     *                index factory, the value is equivalent to IVF&lt;nLists&gt;,Flat. From ArangoDB 3.12.10 onward,
     *                the {@code "{}"} placeholder is replaced with the number of centroids that {@code nLists}
     *                resolves to. For more information on how to create these custom indexes, see the
     *                <a href="https://github.com/facebookresearch/faiss/wiki/The-index-factory">Faiss Wiki</a>.
     * @return this
     */
    public VectorIndexParams factory(String factory) {
        this.factory = factory;
        return this;
    }

    public Metric getMetric() {
        return metric;
    }

    /**
     * @param metric The measure for calculating the vector similarity.
     * @return this
     */
    public VectorIndexParams metric(Metric metric) {
        this.metric = metric;
        return this;
    }

    /**
     * @deprecated since ArangoDB 3.12.10, use {@link #getNLists()}.
     */
    @Deprecated
    @JsonIgnore
    public Integer getnLists() {
        return nLists instanceof NLists.NumericNLists ? ((NLists.NumericNLists) nLists).get() : null;
    }

    /**
     * Returns the fixed or scaling centroid configuration.
     *
     * @return fixed or scaling centroid configuration, or {@code null} if omitted
     * @since ArangoDB 3.12.10
     */
    @JsonIgnore
    public NLists getNLists() {
        return nLists;
    }

    /**
     * @param nLists The fixed or scaling number of Voronoi cells (centroids). Scaling configuration is available
     *               from ArangoDB 3.12.10 and is resolved by the server for each shard. From ArangoDB 3.12.10 onward,
     *               this option may be omitted.
     * @return this
     * @since ArangoDB 3.12.10
     */
    public VectorIndexParams nLists(final NLists nLists) {
        this.nLists = nLists;
        return this;
    }

    /**
     * @deprecated since ArangoDB 3.12.10, use {@link #nLists(NLists)}. Calls written as {@code nLists(null)} are
     *             source-ambiguous; cast the null or use the typed overload.
     */
    @Deprecated
    public VectorIndexParams nLists(Integer nLists) {
        this.nLists = NLists.fixed(nLists);
        return this;
    }

    public Integer getNumberOfDocsPerCentroid() {
        return numberOfDocsPerCentroid;
    }

    /**
     * @param numberOfDocsPerCentroid How many vectors per centroid to include in the random sample used for training.
     *                                The server default is 100.
     * @return this
     * @since ArangoDB 3.12.10
     */
    public VectorIndexParams numberOfDocsPerCentroid(final Integer numberOfDocsPerCentroid) {
        this.numberOfDocsPerCentroid = numberOfDocsPerCentroid;
        return this;
    }

    public Integer getTrainingIterations() {
        return trainingIterations;
    }

    /**
     * @param trainingIterations The number of iterations in the training process. The default is 25. Smaller values
     *                           lead to a faster index creation but may yield worse search results.
     * @return this
     */
    public VectorIndexParams trainingIterations(Integer trainingIterations) {
        this.trainingIterations = trainingIterations;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VectorIndexParams that = (VectorIndexParams) o;
        return Objects.equals(defaultNProbe, that.defaultNProbe) && Objects.equals(dimension, that.dimension) && Objects.equals(factory, that.factory) && metric == that.metric && Objects.equals(nLists, that.nLists) && Objects.equals(numberOfDocsPerCentroid, that.numberOfDocsPerCentroid) && Objects.equals(trainingIterations, that.trainingIterations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultNProbe, dimension, factory, metric, nLists, numberOfDocsPerCentroid, trainingIterations);
    }

    public enum Metric {
        /**
         * Angular similarity. Vectors are automatically normalized before insertion and search.
         */
        cosine,

        /**
         * Similarity in terms of angle and magnitude. Vectors are not normalized, making it faster than cosine.
         */
        innerProduct,

        /**
         * Euclidean distance.
         */
        l2
    }
}
