package com.arangodb.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class ShadedProxy {
    private static final Logger LOG = LoggerFactory.getLogger(ShadedProxy.class);
    private static final ClassLoader classLoader = ShadedProxy.class.getClassLoader();

    @SuppressWarnings("unchecked")
    public static <T> T of(Class<T> i, Object target) {
        return (T) Proxy.newProxyInstance(
                classLoader,
                new Class[]{i},
                new ShadedInvocationHandler(i, target));
    }

    public static Optional<Object> getTarget(Object o) {
        if (Proxy.isProxyClass(o.getClass())) {
            InvocationHandler h = Proxy.getInvocationHandler(o);
            if (h instanceof ShadedInvocationHandler) {
                return Optional.of(((ShadedInvocationHandler) h).target);
            }
        }
        return Optional.empty();
    }

    private static class ShadedInvocationHandler implements InvocationHandler {
        private final Map<ProxyMethod, Method> targetMethods = new HashMap<>();
        private final Map<ProxyMethod, Class<?>> proxiedReturnTypes = new HashMap<>();
        private final Object target;

        ShadedInvocationHandler(Class<?> i, Object target) {
            this.target = target;
            Map<ProxyMethod, Method> iMethods = new HashMap<>();
            for (Method method : i.getDeclaredMethods()) {
                iMethods.put(new ProxyMethod(method), method);
            }

            Method[] methods;
            if (target instanceof Class<?>) {
                // proxy for static methods
                methods = ((Class<?>) target).getMethods();
            } else {
                methods = target.getClass().getMethods();
            }

            for (Method method : methods) {
                ProxyMethod pm = new ProxyMethod(method);
                Method iMethod = iMethods.get(pm);
                if (iMethod != null) {
                    LOG.trace("adding {}", iMethod);
                    targetMethods.put(pm, method);
                    Class<?> mRet = method.getReturnType();
                    Class<?> iRet = iMethod.getReturnType();
                    if (!mRet.equals(iRet)) {
                        LOG.trace("adding proxied return type {}", iRet);
                        proxiedReturnTypes.put(pm, iRet);
                    }
                }
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            ProxyMethod pm = new ProxyMethod(method);
            Method targetMethod = targetMethods.get(pm);
            LOG.trace("Proxying invocation \n\t of: {} \n\t to: {}", method, targetMethod);
            Class<?> returnProxy = proxiedReturnTypes.get(pm);
            Object[] realArgs;
            if (args == null) {
                realArgs = null;
            } else {
                realArgs = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    realArgs[i] = ShadedProxy.getTarget(args[i]).orElse(args[i]);
                }
            }
            Object res = targetMethod.invoke(target, realArgs);
            if (returnProxy != null) {
                LOG.trace("proxying return type \n\t of: {} \n\t to: {}", targetMethod.getReturnType(), returnProxy);
                return ShadedProxy.of(returnProxy, res);
            } else {
                return res;
            }
        }

        private static class ProxyMethod {
            private final String name;
            private final String simpleReturnType;
            private final String[] simpleParameterTypes;

            public ProxyMethod(Method method) {
                name = method.getName();
                simpleReturnType = method.getReturnType().getSimpleName();
                simpleParameterTypes = new String[method.getParameterTypes().length];
                for (int i = 0; i < method.getParameterTypes().length; i++) {
                    simpleParameterTypes[i] = method.getParameterTypes()[i].getSimpleName();
                }
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ProxyMethod that = (ProxyMethod) o;
                return Objects.equals(name, that.name) && Objects.equals(simpleReturnType, that.simpleReturnType) && Arrays.equals(simpleParameterTypes, that.simpleParameterTypes);
            }

            @Override
            public int hashCode() {
                int result = Objects.hash(name, simpleReturnType);
                result = 31 * result + Arrays.hashCode(simpleParameterTypes);
                return result;
            }
        }
    }

}
