package com.arangodb.entity;


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
    private Integer nLists;
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
     *                <p>
     *                The base index must be an inverted file (IVF) to work with ArangoDB. If you don’t specify an
     *                index factory, the value is equivalent to IVF<nLists>,Flat. For more information on how to
     *                create these custom indexes, see the
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

    public Integer getnLists() {
        return nLists;
    }

    /**
     * @param nLists The number of Voronoi cells to partition the vector space into, respectively the number of
     *               centroids in the index. What value to choose depends on the data distribution and chosen metric.
     *               According to The Faiss library paper , it should be around 15 * sqrt(N) where N is the number of
     *               documents in the collection, respectively the number of documents in the shard for cluster
     *               deployments. A bigger value produces more correct results but increases the training time and thus
     *               how long it takes to build the index. It cannot be bigger than the number of documents.
     * @return this
     */
    public VectorIndexParams nLists(Integer nLists) {
        this.nLists = nLists;
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
        return Objects.equals(defaultNProbe, that.defaultNProbe) && Objects.equals(dimension, that.dimension) && Objects.equals(factory, that.factory) && metric == that.metric && Objects.equals(nLists, that.nLists) && Objects.equals(trainingIterations, that.trainingIterations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultNProbe, dimension, factory, metric, nLists, trainingIterations);
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
