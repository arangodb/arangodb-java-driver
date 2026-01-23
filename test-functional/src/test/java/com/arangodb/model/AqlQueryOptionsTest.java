package com.arangodb.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AqlQueryOptionsTest {

    @Test
    void cloneable() {
        List<String> rules = Arrays.asList("foo", "bar");
        AqlQueryOptions options = new AqlQueryOptions()
                .cache(true)
                .stream(true)
                .usePlanCache(true)
                .rules(rules)
                .shardIds("a", "b");
        AqlQueryOptions clone = options.clone();
        assertThat(clone.getCache()).isEqualTo(options.getCache());
        assertThat(clone.getStream()).isEqualTo(options.getStream());
        assertThat(clone.getUsePlanCache()).isEqualTo(options.getUsePlanCache());
        assertThat(clone.getRules())
                .isEqualTo(options.getRules())
                .isNotSameAs(options.getRules());
        assertThat(clone.getShardIds())
                .isEqualTo(options.getShardIds())
                .isNotSameAs(options.getShardIds());
    }

}
