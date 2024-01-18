package com.arangodb.internal.serde;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class JacksonUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JacksonUtils.class);

    private JacksonUtils() {
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
        // using reflection because these configuration are not supported in older Jackson versions
        if (isAtLeastVersion(jf, 2, 15)) {
            LOG.debug("Configuring StreamReadConstraints ...");
            List<Invocation> readConf = new ArrayList<>();
            readConf.add(new Invocation("maxNumberLength", int.class, Integer.MAX_VALUE));
            readConf.add(new Invocation("maxStringLength", int.class, Integer.MAX_VALUE));
            readConf.add(new Invocation("maxNestingDepth", int.class, Integer.MAX_VALUE));
            if (isAtLeastVersion(jf, 2, 16)) {
                readConf.add(new Invocation("maxNameLength", int.class, Integer.MAX_VALUE));
                readConf.add(new Invocation("maxDocumentLength", long.class, Long.MAX_VALUE));
            } else {
                LOG.debug("Skipping configuring StreamReadConstraints maxNameLength");
                LOG.debug("Skipping configuring StreamReadConstraints maxDocumentLength");
            }
            configureStreamConstraints(jf, "StreamReadConstraints", readConf);
        } else {
            LOG.debug("Skipping configuring StreamReadConstraints");
        }

        if (isAtLeastVersion(jf, 2, 16)) {
            LOG.debug("Configuring StreamWriteConstraints ...");
            List<Invocation> writeConf = new ArrayList<>();
            writeConf.add(new Invocation("maxNestingDepth", int.class, Integer.MAX_VALUE));
            configureStreamConstraints(jf, "StreamWriteConstraints", writeConf);
        } else {
            LOG.debug("Skipping configuring StreamWriteConstraints");
        }
    }

    private static boolean isAtLeastVersion(Object jf, int major, int minor) throws Exception {
        Class<?> packageVersionClass = Class.forName(jf.getClass().getPackage().getName() + ".json.PackageVersion");
        Object version = packageVersionClass.getDeclaredField("VERSION").get(null);

        Class<?> versionClass = Class.forName(jf.getClass().getPackage().getName() + ".Version");
        int currentMajor = (int) versionClass.getDeclaredMethod("getMajorVersion").invoke(version);
        int currentMinor = (int) versionClass.getDeclaredMethod("getMinorVersion").invoke(version);

        LOG.debug("Detected Jackson version: {}.{}", currentMajor, currentMinor);

        return currentMajor > major || (currentMajor == major && currentMinor >= minor);
    }

    private static void configureStreamConstraints(Object jf, String className, List<Invocation> conf) throws Exception {
        // get pkg name dynamically, to support shaded Jackson
        String basePkg = jf.getClass().getPackage().getName();
        Class<?> streamConstraintsClass = Class.forName(basePkg + "." + className);
        Class<?> builderClass = Class.forName(basePkg + "." + className + "$Builder");
        Method buildMethod = builderClass.getDeclaredMethod("build");
        Method builderMethod = streamConstraintsClass.getDeclaredMethod("builder");
        Object builder = builderMethod.invoke(null);
        for (Invocation i : conf) {
            Method method = builderClass.getDeclaredMethod(i.method, i.argType);
            method.invoke(builder, i.arg);
        }
        Object streamReadConstraints = buildMethod.invoke(builder);
        Method setStreamReadConstraintsMethod = jf.getClass().getDeclaredMethod("set" + className, streamConstraintsClass);
        setStreamReadConstraintsMethod.invoke(jf, streamReadConstraints);
    }

    private static class Invocation {
        final String method;
        final Class<?> argType;
        final Object arg;

        Invocation(String method, Class<?> argType, Object arg) {
            this.method = method;
            this.argType = argType;
            this.arg = arg;
        }
    }
}
