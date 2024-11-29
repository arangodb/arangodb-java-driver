package com.arangodb;

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
    @State(Scope.Benchmark)
    public static class Data {
        public final byte[] vpack;
        public final byte[] json;
        public final RawBytes rawJsonBytes;
        public final RawBytes rawVPackBytes;
        public final RawJson rawJson;

        public Data() {
            ObjectMapper jsonMapper = new ObjectMapper();
            VPackMapper vpackMapper = new VPackMapper();

            try {
                String str = new String(Files.readAllBytes(
                        Paths.get(SerdeBench.class.getResource("/api-docs.json").toURI())));
                JsonNode jn = jsonMapper.readTree(str);

                json = jsonMapper.writeValueAsBytes(jn);
                vpack = vpackMapper.writeValueAsBytes(jn);
                rawJsonBytes = RawBytes.of(json);
                rawVPackBytes = RawBytes.of(vpack);
                rawJson = RawJson.of(jsonMapper.writeValueAsString(json));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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

}
