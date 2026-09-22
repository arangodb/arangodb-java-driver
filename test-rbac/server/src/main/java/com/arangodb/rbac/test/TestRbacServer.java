/* Licensed under the Apache License, Version 2.0. See the repository LICENSE. */
package com.arangodb.rbac.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Disposable RBAC server for the curl demo. ArangoDB authenticates the JWT. */
public final class TestRbacServer implements AutoCloseable {
    private static final String EVALUATE_PATH = "/_integration/authorization/v1/evaluate-token-many";
    private static final String ADMIN_TOKEN = "driver-rbac-test-only";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int MAX_BODY = 1_048_576;

    private final HttpServer server;
    private final HttpServer administrator;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final ConcurrentMap<String, Policy> policies = new ConcurrentHashMap<>();

    private record Rule(String action, String resource, String effect) {
        boolean matches(String requestedAction, String requestedResource) {
            return matchesPattern(action, requestedAction) && matchesPattern(resource, requestedResource);
        }
    }

    private record Policy(String defaultEffect, List<Rule> rules) {
        String evaluate(String action, String resource) {
            boolean allowed = false;
            for (Rule rule : rules) {
                if (rule.matches(action, resource)) {
                    if ("Deny".equals(rule.effect())) return "Deny";
                    allowed = true;
                }
            }
            return allowed ? "Allow" : defaultEffect;
        }
    }

    private static boolean matchesPattern(String pattern, String value) {
        return pattern.endsWith("*") ? value.startsWith(pattern.substring(0, pattern.length() - 1))
                : pattern.equals(value);
    }

    public TestRbacServer(String bindAddress, int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(bindAddress, port), 64);
        try {
            administrator = HttpServer.create(new InetSocketAddress("127.0.0.1", 18081), 16);
        } catch (IOException e) {
            server.stop(0);
            executor.shutdownNow();
            throw e;
        }
        server.createContext("/", this::handle);
        administrator.createContext("/", this::handleAdmin);
        server.setExecutor(executor);
        administrator.setExecutor(executor);
        server.start();
        administrator.start();
    }

    private void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            if ("GET".equals(exchange.getRequestMethod()) && "/health".equals(path)) {
                reply(exchange, 200, Map.of("status", "ok", "testOnly", true,
                        "service", "arangodb-java-driver/test-rbac", "pid", ProcessHandle.current().pid()));
            } else if ("POST".equals(exchange.getRequestMethod()) && EVALUATE_PATH.equals(path)) {
                evaluate(exchange);
            } else {
                reply(exchange, 404, Map.of("error", "Unknown route or method"));
            }
        }
    }

    private void evaluate(HttpExchange exchange) throws IOException {
        byte[] bytes = exchange.getRequestBody().readNBytes(MAX_BODY + 1);
        if (bytes.length > MAX_BODY) {
            reply(exchange, 413, Map.of("error", "Request too large"));
            return;
        }
        try {
            JsonNode request = JSON.readTree(bytes);
            if (request == null || !request.isObject() || !request.path("token").isTextual()
                    || !request.path("items").isArray() || request.path("items").size() > 10_000) {
                throw new IllegalArgumentException("Invalid evaluation request");
            }
            List<Map<String, String>> results = new ArrayList<>();
            String user = username(request.path("token").textValue());
            Policy policy = policies.get(user);
            boolean allowed = true;
            for (JsonNode item : request.path("items")) {
                if (!item.isObject() || !item.path("action").isTextual()
                        || !item.path("resource").isTextual()) {
                    throw new IllegalArgumentException("Invalid evaluation item");
                }
                String itemEffect = "root".equals(user) ? "Allow"
                        : policy == null ? "Deny"
                        : policy.evaluate(item.path("action").textValue(), item.path("resource").textValue());
                allowed &= "Allow".equals(itemEffect);
                results.add(Map.of("effect", itemEffect,
                        "message", "Allow".equals(itemEffect) ? "" : "denied by test policy"));
            }
            String effect = allowed && !user.isEmpty() ? "Allow" : "Deny";
            String message = "Allow".equals(effect) ? "" : "denied by test policy";
            reply(exchange, 200, Map.of("effect", effect, "message", message, "items", results));
        } catch (IOException | IllegalArgumentException e) {
            reply(exchange, 400, Map.of("error", "Invalid evaluation request"));
        }
    }

    private void handleAdmin(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!ADMIN_TOKEN.equals(exchange.getRequestHeaders().getFirst("X-Rbac-Test-Admin"))) {
                reply(exchange, 401, Map.of("error", "Invalid administration token"));
                return;
            }
            if (!"PUT".equals(exchange.getRequestMethod())
                    || !"/_test/policy".equals(exchange.getRequestURI().getPath())) {
                reply(exchange, 404, Map.of("error", "Unknown route or method"));
                return;
            }
            byte[] bytes = exchange.getRequestBody().readNBytes(MAX_BODY + 1);
            if (bytes.length > MAX_BODY) {
                reply(exchange, 413, Map.of("error", "Request too large"));
                return;
            }
            try {
                JsonNode request = JSON.readTree(bytes);
                String user = requiredText(request, "username");
                if ("root".equals(user)) {
                    reply(exchange, 403, Map.of("error", "root policy cannot be changed"));
                    return;
                }
                JsonNode configured = request.path("policy");
                String defaultEffect = effect(requiredText(configured, "defaultEffect"));
                JsonNode rulesNode = configured.path("rules");
                if (!rulesNode.isArray() || rulesNode.size() > 100) {
                    throw new IllegalArgumentException("Invalid rules");
                }
                List<Rule> rules = new ArrayList<>();
                for (JsonNode rule : rulesNode) {
                    rules.add(new Rule(requiredText(rule, "action"), requiredText(rule, "resource"),
                            effect(requiredText(rule, "effect"))));
                }
                policies.put(user, new Policy(defaultEffect, List.copyOf(rules)));
                reply(exchange, 200, Map.of("ok", true));
            } catch (IOException | IllegalArgumentException e) {
                reply(exchange, 400, Map.of("error", "Invalid policy"));
            }
        }
    }

    private static String requiredText(JsonNode node, String name) {
        JsonNode value = node.path(name);
        if (!value.isTextual() || value.textValue().isBlank()) {
            throw new IllegalArgumentException("Missing field");
        }
        return value.textValue();
    }

    private static String effect(String value) {
        if (!"Allow".equals(value) && !"Deny".equals(value)) {
            throw new IllegalArgumentException("Invalid effect");
        }
        return value;
    }

    private static String username(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3 || parts[0].isEmpty() || parts[2].isEmpty()) {
                return "";
            }
            JsonNode claims = JSON.readTree(Base64.getUrlDecoder().decode(parts[1]));
            return claims != null && claims.path("preferred_username").isTextual()
                    ? claims.path("preferred_username").textValue() : "";
        } catch (IOException | IllegalArgumentException e) {
            return "";
        }
    }

    private static void reply(HttpExchange exchange, int status, Object value) throws IOException {
        byte[] body = JSON.writeValueAsBytes(value);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
    }

    @Override
    public void close() {
        server.stop(0);
        administrator.stop(0);
        executor.shutdownNow();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 0) {
            throw new IllegalArgumentException("Usage: TestRbacServer");
        }
        String address = System.getenv().getOrDefault("RBAC_BIND_ADDRESS", "127.0.0.1");
        try (TestRbacServer server = new TestRbacServer(address, 18080)) {
            System.err.println("Test RBAC server listening on " + address + ":18080; root always allowed");
            new CountDownLatch(1).await();
        }
    }
}
