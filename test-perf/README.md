# Serde performance tests

```
mvn clean package -am -pl test-perf
java -cp test-perf/target/benchmarks.jar com.arangodb.SerdeBench
```

## 19/12/2024

- `main f613d3d6`
- `benchmark/base 1e45f8c4`

```
Benchmark	                                        Mode	Cnt	      Score	Score		main/base
SerdeBench.deserializeDocsJson	                    avgt	10	      0.155	0.149		0.961290322580645
SerdeBench.deserializeDocsVPack	                    avgt	10	      0.209	0.126		0.602870813397129
SerdeBench.extractBytesJson	                        avgt	10	      2.705	0.297		0.109796672828096
SerdeBench.extractBytesVPack	                    avgt	10	       1.12	0.133		0.11875
SerdeBench.rawJsonDeser	                            avgt	10	      6.016	6.116		1.01662234042553
SerdeBench.rawJsonSer	                            avgt	10	      7.711	7.222		0.936584100635456
```
