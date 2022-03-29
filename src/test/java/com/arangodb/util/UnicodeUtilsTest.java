package com.arangodb.util;

import com.arangodb.internal.util.EncodeUtils;
import org.graalvm.home.Version;
import org.graalvm.nativeimage.ImageInfo;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


class UnicodeUtilsTest {

    private static Context context;
    private static Value jsEncoder;
    private static Value jsNormalizer;
    private static final String encodeFn = "(function encode(x){return encodeURIComponent(x);})";
    private static final String normalizeFn = "(function normalize(x){return x.normalize('NFC');})";

    @BeforeAll
    static void beforeClass() {
        assumeFalse(ImageInfo.inImageCode(), "skipped in native mode");
        assumeTrue(Version.getCurrent().isRelease(), "This test requires GraalVM");
        context = Context.create();
        jsEncoder = context.eval("js", encodeFn);
        jsNormalizer = context.eval("js", normalizeFn);
    }

    @AfterAll
    static void afterClass() {
        if (context != null)
            context.close();
    }

    @Test
    void normalizeShouldBehaveAsJs() {
        for (int i = 0; i < 10_000; i++) {
            String value = TestUtils.generateRandomDbName(100, true);
            String jsNormalized = jsNormalizer.execute(value).as(String.class);
            String javaNormalized = UnicodeUtils.normalize(value);
            assertThat(javaNormalized).isEqualTo(jsNormalized);
        }
    }

    @Test
    void encodeURIComponentShouldBehaveAsJs() {
        for (int i = 0; i < 10_000; i++) {
            String value = TestUtils.generateRandomDbName(100, true);
            String jsEncoded = jsEncoder.execute(value).as(String.class);
            String driverJavaEncoded = EncodeUtils.encodeURIComponent(value);
            assertThat(driverJavaEncoded).isEqualTo(jsEncoded);
        }
    }

}