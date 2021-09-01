package com.arangodb.util;

import com.arangodb.internal.util.EncodeUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeTrue;

public class UnicodeUtilsTest {

    private static Context context;
    private static Value jsEncoder;
    private static Value jsNormalizer;
    private static final String encodeFn = "(function encode(x){return encodeURIComponent(x);})";
    private static final String normalizeFn = "(function normalize(x){return x.normalize('NFC');})";

    @BeforeClass
    public static void beforeClass() {
        assumeTrue("This test requires GraalVM", org.graalvm.home.Version.getCurrent().isRelease());
        context = Context.create();
        jsEncoder = context.eval("js", encodeFn);
        jsNormalizer = context.eval("js", normalizeFn);
    }

    @AfterClass
    public static void afterClass() {
        if (context != null)
            context.close();
    }

    @Test
    public void normalizeShouldBehaveAsJs() {
        for (int i = 0; i < 10_000; i++) {
            String value = TestUtils.generateRandomDbName(100, true);
            String jsNormalized = jsNormalizer.execute(value).as(String.class);
            String javaNormalized = UnicodeUtils.normalize(value);
            assertThat(javaNormalized, is(jsNormalized));
        }
    }

    @Test
    public void encodeURIComponentShouldBehaveAsJs() {
        for (int i = 0; i < 10_000; i++) {
            String value = TestUtils.generateRandomDbName(100, true);
            String jsEncoded = jsEncoder.execute(value).as(String.class);
            String driverJavaEncoded = EncodeUtils.encodeURIComponent(value);
            assertThat(driverJavaEncoded, is(jsEncoded));
        }
    }

}