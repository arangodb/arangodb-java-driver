package com.arangodb;

import com.arangodb.entity.MultiDocumentEntity;
import com.arangodb.internal.ArangoCollectionImpl;
import com.arangodb.internal.ArangoDatabaseImpl;
import com.arangodb.internal.ArangoExecutor;
import com.arangodb.internal.InternalResponse;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.serde.InternalSerdeProvider;
import com.arangodb.jackson.dataformat.velocypack.VPackMapper;
import com.arangodb.util.RawBytes;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 8, time = 1)
@Measurement(iterations = 10, time = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class SerdeBench {
    public static class MyCol extends ArangoCollectionImpl {
        static ArangoDB jsonAdb = new ArangoDB.Builder()
                .host("127.0.0.1", 8529)
                .protocol(Protocol.HTTP_JSON)
                .build();

        static ArangoDB vpackAdb = new ArangoDB.Builder()
                .host("127.0.0.1", 8529)
                .protocol(Protocol.HTTP_VPACK)
                .build();

        private MyCol(ArangoDB adb) {
            super((ArangoDatabaseImpl) adb.db(), "foo");
        }

        public static MyCol ofJson() {
            return new MyCol(jsonAdb);
        }

        public static MyCol ofVpack() {
            return new MyCol(vpackAdb);
        }

        @Override
        public <T> ArangoExecutor.ResponseDeserializer<MultiDocumentEntity<T>> getDocumentsResponseDeserializer(Class<T> type) {
            return super.getDocumentsResponseDeserializer(type);
        }
    }

    @State(Scope.Benchmark)
    public static class Data {
        public final byte[] vpack;
        public final byte[] json;
        public final RawBytes rawJsonBytes;
        public final RawBytes rawVPackBytes;
        public final RawJson rawJson;
        public final MyCol jsonCol = MyCol.ofJson();
        public final MyCol vpackCol = MyCol.ofVpack();
        public final InternalResponse jsonResp = new InternalResponse();
        public final InternalResponse vpackResp = new InternalResponse();

        public Data() {
            ObjectMapper jsonMapper = new ObjectMapper();
            VPackMapper vpackMapper = new VPackMapper();

            try {
                JsonNode jn = readFile("/api-docs.json", jsonMapper);
                json = jsonMapper.writeValueAsBytes(jn);
                vpack = vpackMapper.writeValueAsBytes(jn);
                rawJsonBytes = RawBytes.of(json);
                rawVPackBytes = RawBytes.of(vpack);
                rawJson = RawJson.of(jsonMapper.writeValueAsString(jsonMapper.readTree(json)));

                JsonNode docs = readFile("/multi-docs.json", jsonMapper);
                jsonResp.setResponseCode(200);
                jsonResp.setBody(jsonMapper.writeValueAsBytes(docs));
                vpackResp.setResponseCode(200);
                vpackResp.setBody(vpackMapper.writeValueAsBytes(docs));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private JsonNode readFile(String filename, ObjectMapper mapper) throws IOException, URISyntaxException {
            String str = new String(Files.readAllBytes(
                    Paths.get(SerdeBench.class.getResource(filename).toURI())));
            return mapper.readTree(str);
        }
    }

    public static void main(String[] args) throws RunnerException, IOException {
        String datetime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        Path target = Files.createDirectories(Paths.get("target", "jmh-result"));

        ArrayList<String> jvmArgs = new ArrayList<>();
        jvmArgs.add("-Xms256m");
        jvmArgs.add("-Xmx256m");
        if (Integer.parseInt(System.getProperty("java.version").split("\\.")[0]) >= 11) {
            jvmArgs.add("-XX:StartFlightRecording=filename=" + target.resolve(datetime + ".jfr") + ",settings=profile");
        }

        Options opt = new OptionsBuilder()
                .include(SerdeBench.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .jvmArgs(jvmArgs.toArray(new String[0]))
                .resultFormat(ResultFormatType.JSON)
                .result(target.resolve(datetime + ".json").toString())
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public void rawJsonDeser(Data data, Blackhole bh) {
        InternalSerde serde = new InternalSerdeProvider(ContentType.VPACK).create();
        bh.consume(
                serde.deserialize(data.vpack, RawJson.class)
        );
    }

    @Benchmark
    public void rawJsonSer(Data data, Blackhole bh) {
        InternalSerde serde = new InternalSerdeProvider(ContentType.VPACK).create();
        bh.consume(
                serde.serialize(data.rawJson)
        );
    }

    @Benchmark
    public void extractBytesVPack(Data data, Blackhole bh) {
        InternalSerde serde = new InternalSerdeProvider(ContentType.VPACK).create();
        bh.consume(
                serde.extract(data.vpack, "/definitions/put_api_simple_remove_by_example_opts")
        );
    }

    @Benchmark
    public void extractBytesJson(Data data, Blackhole bh) {
        InternalSerde serde = new InternalSerdeProvider(ContentType.JSON).create();
        bh.consume(
                serde.extract(data.json, "/definitions/put_api_simple_remove_by_example_opts")
        );
    }

    @Benchmark
    public void deserializeDocsJson(Data data, Blackhole bh) {
        bh.consume(
                data.jsonCol.getDocumentsResponseDeserializer(RawBytes.class).deserialize(data.jsonResp)
        );
    }

    @Benchmark
    public void deserializeDocsVPack(Data data, Blackhole bh) {
        bh.consume(
                data.vpackCol.getDocumentsResponseDeserializer(RawBytes.class).deserialize(data.vpackResp)
        );
    }

}
