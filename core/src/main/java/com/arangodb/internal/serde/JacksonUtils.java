package com.arangodb.internal.serde;

import com.arangodb.internal.ShadedProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JacksonUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JacksonUtils.class);

    private JacksonUtils() {
    }

    public interface Version {
        int getMajorVersion();

        int getMinorVersion();

        String toString();
    }

    public interface StreamReadConstraints {

        interface Static {
            Builder builder();
        }

        interface Builder {
            Builder maxNumberLength(final int maxNumLen);

            Builder maxStringLength(int maxStringLen);

            Builder maxNestingDepth(int maxNestingDepth);

            Builder maxNameLength(int maxNameLen);

            Builder maxDocumentLength(long maxDocLen);

            StreamReadConstraints build();
        }
    }

    public interface StreamWriteConstraints {
        interface Static {
            Builder builder();
        }

        interface Builder {
            Builder maxNestingDepth(int maxNestingDepth);

            StreamWriteConstraints build();
        }
    }

    public interface JsonFactory {
        Version version();

        @SuppressWarnings("UnusedReturnValue")
        JsonFactory setStreamReadConstraints(StreamReadConstraints src);

        @SuppressWarnings("UnusedReturnValue")
        JsonFactory setStreamWriteConstraints(StreamWriteConstraints swc);
    }

    /**
     * Configure JsonFactory with permissive StreamReadConstraints and StreamWriteConstraints.
     * It uses reflection to avoid compilation errors with older Jackson versions.
     * It uses dynamic package names to be compatible with shaded Jackson.
     *
     * @param jf JsonFactory to configure
     */
    public static void tryConfigureJsonFactory(Object jf) {
        try {
            configureJsonFactory(jf);
        } catch (Throwable t) {
            LOG.warn("Got exception while configuring JsonFactory, skipping...", t);
        }
    }

    private static void configureJsonFactory(Object jf) throws Exception {
        JsonFactory proxy = ShadedProxy.of(JsonFactory.class, jf);
        Version version = proxy.version();
        LOG.debug("Detected Jackson version: {}", version);

        // get pkg name dynamically, to support shaded Jackson
        String basePkg = jf.getClass().getPackage().getName();

        if (isAtLeastVersion(version, 2, 15)) {
            Class<?> srcClass = Class.forName(basePkg + "." + StreamReadConstraints.class.getSimpleName());
            StreamReadConstraints.Builder builder = ShadedProxy.of(StreamReadConstraints.Static.class, srcClass)
                    .builder()
                    .maxNumberLength(Integer.MAX_VALUE)
                    .maxStringLength(Integer.MAX_VALUE)
                    .maxNestingDepth(Integer.MAX_VALUE);
            if (isAtLeastVersion(version, 2, 16)) {
                builder = builder
                        .maxNameLength(Integer.MAX_VALUE)
                        .maxDocumentLength(Long.MAX_VALUE);
            } else {
                LOG.debug("Skipping configuring StreamReadConstraints maxNameLength");
                LOG.debug("Skipping configuring StreamReadConstraints maxDocumentLength");
            }
            proxy.setStreamReadConstraints(builder.build());
        } else {
            LOG.debug("Skipping configuring StreamReadConstraints");
        }

        if (isAtLeastVersion(version, 2, 16)) {
            LOG.debug("Configuring StreamWriteConstraints ...");
            Class<?> swcClass = Class.forName(basePkg + "." + StreamWriteConstraints.class.getSimpleName());
            StreamWriteConstraints swc = ShadedProxy.of(StreamWriteConstraints.Static.class, swcClass)
                    .builder()
                    .maxNestingDepth(Integer.MAX_VALUE)
                    .build();
            proxy.setStreamWriteConstraints(swc);
        } else {
            LOG.debug("Skipping configuring StreamWriteConstraints");
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static boolean isAtLeastVersion(Version version, int major, int minor) {
        int currentMajor = version.getMajorVersion();
        int currentMinor = version.getMinorVersion();
        return currentMajor > major || (currentMajor == major && currentMinor >= minor);
    }

}
