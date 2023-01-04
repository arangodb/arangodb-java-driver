package com.arangodb.config;

import org.eclipse.microprofile.config.spi.Converter;

import java.util.Objects;

public class HostConverter implements Converter<Host> {
    @Override
    public Host convert(String value) throws IllegalArgumentException, NullPointerException {
        Objects.requireNonNull(value);
        final String[] split = value.split(":");
        if (split.length != 2) {
            throw new IllegalArgumentException("Could not parse host. Expected hostname:port, but got: " + value);
        }
        return new Host(split[0], Integer.parseInt(split[1]));
    }
}
