package app;

import app.analytics.AnalyticsService;
import app.analytics.ForecastResult;
import app.analytics.KeywordResult;
import app.analytics.SentimentResult;
import app.cloud.ClusterPlan;
import app.cloud.DockerOrchestrator;
import app.cloud.KubernetesPlanner;
import app.cloud.NodeCapacity;
import app.cloud.ResourceAllocator;
import app.cloud.Workload;
import app.diagnostics.ConfigurationInspector;
import app.json.JsonArray;
import app.json.JsonObject;
import app.json.JsonParser;
import app.json.JsonUtil;
import app.json.JsonValue;
import app.observability.MetricsRegistry;
import app.observability.OpenTelemetryScaffolding;
import app.observability.StructuredLogger;
import app.policy.PolicyDecision;
import app.policy.PolicyEnforcer;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class StandaloneServices {
    private static final List<User> users = Collections.synchronizedList(new ArrayList<>());
    private static final List<Payment> payments = Collections.synchronizedList(new ArrayList<>());
    private static final List<Notification> notifications = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, String> validUsers = Map.of(
            "user1", "password1",
            "user2", "password2"
    );
    private static final Instant startedAt = Instant.now();
    private static final AnalyticsService analyticsService = new AnalyticsService();
    private static final ResourceAllocator resourceAllocator = new ResourceAllocator();
    private static final KubernetesPlanner kubernetesPlanner = new KubernetesPlanner();
    private static final DockerOrchestrator dockerOrchestrator = new DockerOrchestrator();
    private static final PolicyEnforcer policyEnforcer = new PolicyEnforcer();

    private static final AtomicInteger userIds = new AtomicInteger(0);
    private static final AtomicInteger paymentIds = new AtomicInteger(0);
    private static final AtomicInteger notificationIds = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        if (hasFlag(args, "--explain")) {
            System.out.println(ConfigurationInspector.inspect().toJson());
            return;
        }
        if (hasFlag(args, "--demo")) {
            DemoRunner.run();
            return;
        }
        OpenTelemetryScaffolding.initialize().ifPresent(System.out::println);
        startServer(8080, new UserHandler());
        startServer(8081, new AuthHandler());
        startServer(8082, new PaymentHandler());
        startServer(8083, new NotificationHandler());
        startServer(8084, new AnalyticsHandler());
        startServer(8085, new CloudHandler());

        System.out.println("User Service running at http://localhost:8080/users");
        System.out.println("Auth Service running at http://localhost:8081/login");
        System.out.println("Payment Service running at http://localhost:8082/payments");
        System.out.println("Notification Service running at http://localhost:8083/notifications");
        System.out.println("Analytics Service running at http://localhost:8084/analytics/forecast");
        System.out.println("Cloud Service running at http://localhost:8085/cloud/allocate");

        new CountDownLatch(1).await();
    }

    private static void startServer(int port, HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", handler);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public static ServiceBundle startServers(ServicePorts ports) throws IOException {
        List<HttpServer> servers = new ArrayList<>();
        servers.add(startServerInstance(ports.userPort(), new UserHandler()));
        servers.add(startServerInstance(ports.authPort(), new AuthHandler()));
        servers.add(startServerInstance(ports.paymentPort(), new PaymentHandler()));
        servers.add(startServerInstance(ports.notificationPort(), new NotificationHandler()));
        servers.add(startServerInstance(ports.analyticsPort(), new AnalyticsHandler()));
        servers.add(startServerInstance(ports.cloudPort(), new CloudHandler()));
        return new ServiceBundle(servers, ports);
    }

    private static HttpServer startServerInstance(int port, HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", handler);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        return server;
    }

    private static boolean hasFlag(String[] args, String flag) {
        if (args == null) {
            return false;
        }
        for (String arg : args) {
            if (flag.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    public record ServicePorts(int userPort, int authPort, int paymentPort, int notificationPort, int analyticsPort,
                               int cloudPort) {
        public static ServicePorts defaults() {
            return new ServicePorts(8080, 8081, 8082, 8083, 8084, 8085);
        }
    }

    public record ServiceBundle(List<HttpServer> servers, ServicePorts ports) {
        public ServicePorts resolvedPorts() {
            return new ServicePorts(
                    servers.get(0).getAddress().getPort(),
                    servers.get(1).getAddress().getPort(),
                    servers.get(2).getAddress().getPort(),
                    servers.get(3).getAddress().getPort(),
                    servers.get(4).getAddress().getPort(),
                    servers.get(5).getAddress().getPort()
            );
        }

        public void stopAll() {
            for (HttpServer server : servers) {
                server.stop(0);
            }
        }
    }

    private static class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long startNano = MetricsRegistry.nowMs();
            String path = exchange.getRequestURI().getPath();
            if ("/health".equals(path)) {
                respondJson(exchange, 200, healthPayload("user-service"));
                track(exchange, "user-service", path, 200, startNano);
                return;
            }
            if ("/metrics".equals(path)) {
                if (MetricsRegistry.isEnabled()) {
                    respondJson(exchange, 200, MetricsRegistry.snapshot());
                    track(exchange, "user-service", path, 200, startNano);
                } else {
                    respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                    track(exchange, "user-service", path, 404, startNano);
                }
                return;
            }
            if (path.startsWith("/users")) {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    respondJson(exchange, 200, JsonWriter.writeUsers(users));
                    track(exchange, "user-service", path, 200, startNano);
                } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    Map<String, String> payload = JsonWriter.parseBody(exchange);
                    String name = payload.get("name");
                    String email = payload.get("email");
                    if (name == null || email == null) {
                        respondJson(exchange, 400, "{\"error\":\"name and email are required\"}");
                        track(exchange, "user-service", path, 400, startNano);
                        return;
                    }
                    User created = new User(userIds.incrementAndGet(), name, email);
                    users.add(created);
                    respondJson(exchange, 201, JsonWriter.writeUser(created));
                    track(exchange, "user-service", path, 201, startNano);
                } else if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String[] parts = path.split("/");
                    if (parts.length < 3) {
                        respondJson(exchange, 400, "{\"error\":\"Invalid user id\"}");
                        track(exchange, "user-service", path, 400, startNano);
                        return;
                    }
                    try {
                        int id = Integer.parseInt(parts[2]);
                        boolean removed = users.removeIf(user -> user.id == id);
                        if (removed) {
                            respondJson(exchange, 204, "");
                            track(exchange, "user-service", path, 204, startNano);
                        } else {
                            respondJson(exchange, 404, "{\"error\":\"User not found\"}");
                            track(exchange, "user-service", path, 404, startNano);
                        }
                    } catch (NumberFormatException ex) {
                        respondJson(exchange, 400, "{\"error\":\"Invalid user id\"}");
                        track(exchange, "user-service", path, 400, startNano);
                    }
                } else {
                    respondJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                    track(exchange, "user-service", path, 405, startNano);
                }
            } else {
                respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                track(exchange, "user-service", path, 404, startNano);
            }
        }
    }

    private static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long startNano = MetricsRegistry.nowMs();
            String path = exchange.getRequestURI().getPath();
            if ("/health".equals(path)) {
                respondJson(exchange, 200, healthPayload("auth-service"));
                track(exchange, "auth-service", path, 200, startNano);
                return;
            }
            if ("/metrics".equals(path)) {
                if (MetricsRegistry.isEnabled()) {
                    respondJson(exchange, 200, MetricsRegistry.snapshot());
                    track(exchange, "auth-service", path, 200, startNano);
                } else {
                    respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                    track(exchange, "auth-service", path, 404, startNano);
                }
                return;
            }
            if (!"/login".equals(exchange.getRequestURI().getPath())) {
                respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                track(exchange, "auth-service", "/login", 404, startNano);
                return;
            }
            if ("/metrics".equals(exchange.getRequestURI().getPath())) {
                if (MetricsRegistry.isEnabled()) {
                    respondJson(exchange, 200, MetricsRegistry.snapshot());
                    track(exchange, "auth-service", "/metrics", 200, startNano);
                } else {
                    respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                    track(exchange, "auth-service", "/metrics", 404, startNano);
                }
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                respondJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                track(exchange, "auth-service", "/login", 405, startNano);
                return;
            }
            Map<String, String> payload = JsonWriter.parseBody(exchange);
            String username = payload.get("username");
            String password = payload.get("password");
            if (username != null && password != null && password.equals(validUsers.get(username))) {
                respondJson(exchange, 200, String.format("{\"token\":\"token_for_%s\"}", username));
                track(exchange, "auth-service", "/login", 200, startNano);
            } else {
                respondJson(exchange, 401, "{\"error\":\"Invalid credentials\"}");
                track(exchange, "auth-service", "/login", 401, startNano);
            }
        }
    }

    private static class PaymentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long startNano = MetricsRegistry.nowMs();
            String path = exchange.getRequestURI().getPath();
            if ("/health".equals(path)) {
                respondJson(exchange, 200, healthPayload("payment-service"));
                track(exchange, "payment-service", path, 200, startNano);
                return;
            }
            if ("/metrics".equals(path)) {
                if (MetricsRegistry.isEnabled()) {
                    respondJson(exchange, 200, MetricsRegistry.snapshot());
                    track(exchange, "payment-service", path, 200, startNano);
                } else {
                    respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                    track(exchange, "payment-service", path, 404, startNano);
                }
                return;
            }
            if (!path.startsWith("/payments")) {
                respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                track(exchange, "payment-service", path, 404, startNano);
                return;
            }
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                respondJson(exchange, 200, JsonWriter.writePayments(payments));
                track(exchange, "payment-service", path, 200, startNano);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> payload = JsonWriter.parseBody(exchange);
                try {
                    double amount = Double.parseDouble(payload.getOrDefault("amount", "0"));
                    int userId = Integer.parseInt(payload.getOrDefault("userId", "0"));
                    Payment payment = new Payment(paymentIds.incrementAndGet(), amount, userId);
                    payments.add(payment);
                    respondJson(exchange, 201, JsonWriter.writePayment(payment));
                    track(exchange, "payment-service", path, 201, startNano);
                } catch (NumberFormatException ex) {
                    respondJson(exchange, 400, "{\"error\":\"Invalid payment payload\"}");
                    track(exchange, "payment-service", path, 400, startNano);
                }
                return;
            }
            respondJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            track(exchange, "payment-service", path, 405, startNano);
        }
    }

    private static class NotificationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long startNano = MetricsRegistry.nowMs();
            String path = exchange.getRequestURI().getPath();
            if ("/health".equals(path)) {
                respondJson(exchange, 200, healthPayload("notification-service"));
                track(exchange, "notification-service", path, 200, startNano);
                return;
            }
            if ("/metrics".equals(path)) {
                if (MetricsRegistry.isEnabled()) {
                    respondJson(exchange, 200, MetricsRegistry.snapshot());
                    track(exchange, "notification-service", path, 200, startNano);
                } else {
                    respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                    track(exchange, "notification-service", path, 404, startNano);
                }
                return;
            }
            if (!path.startsWith("/notifications")) {
                respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                track(exchange, "notification-service", path, 404, startNano);
                return;
            }
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                respondJson(exchange, 200, JsonWriter.writeNotifications(notifications));
                track(exchange, "notification-service", path, 200, startNano);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> payload = JsonWriter.parseBody(exchange);
                String message = payload.get("message");
                try {
                    int userId = Integer.parseInt(payload.getOrDefault("userId", "0"));
                    if (message == null) {
                        respondJson(exchange, 400, "{\"error\":\"Message is required\"}");
                        track(exchange, "notification-service", path, 400, startNano);
                        return;
                    }
                    Notification notification = new Notification(notificationIds.incrementAndGet(), message, userId);
                    notifications.add(notification);
                    respondJson(exchange, 201, JsonWriter.writeNotification(notification));
                    track(exchange, "notification-service", path, 201, startNano);
                } catch (NumberFormatException ex) {
                    respondJson(exchange, 400, "{\"error\":\"Invalid notification payload\"}");
                    track(exchange, "notification-service", path, 400, startNano);
                }
                return;
            }
            respondJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            track(exchange, "notification-service", path, 405, startNano);
        }
    }

    private static class AnalyticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long startNano = MetricsRegistry.nowMs();
            String path = exchange.getRequestURI().getPath();
            if ("/health".equals(path)) {
                respondJson(exchange, 200, healthPayload("analytics-service"));
                track(exchange, "analytics-service", path, 200, startNano);
                return;
            }
            if ("/metrics".equals(path)) {
                if (MetricsRegistry.isEnabled()) {
                    respondJson(exchange, 200, MetricsRegistry.snapshot());
                    track(exchange, "analytics-service", path, 200, startNano);
                } else {
                    respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                    track(exchange, "analytics-service", path, 404, startNano);
                }
                return;
            }
            if (!path.startsWith("/analytics")) {
                respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                track(exchange, "analytics-service", path, 404, startNano);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                respondJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                track(exchange, "analytics-service", path, 405, startNano);
                return;
            }
            try {
                JsonObject payload = parseJsonObject(exchange);
                if (path.endsWith("/forecast")) {
                    List<Double> series = parseNumberList(payload, "series");
                    int horizon = (int) payload.getNumber("horizon", 3);
                    ForecastResult result = analyticsService.forecast(series, horizon);
                    Map<String, Object> response = new HashMap<>();
                    response.put("forecast", result.forecast());
                    response.put("slope", result.slope());
                    respondJson(exchange, 200, JsonUtil.toJsonObject(response));
                    track(exchange, "analytics-service", path, 200, startNano);
                    return;
                }
                if (path.endsWith("/sentiment")) {
                    String text = payload.getString("text", "");
                    SentimentResult result = analyticsService.sentiment(text);
                    Map<String, Object> response = new HashMap<>();
                    response.put("score", result.score());
                    response.put("label", result.label());
                    response.put("positives", result.positives());
                    response.put("negatives", result.negatives());
                    respondJson(exchange, 200, JsonUtil.toJsonObject(response));
                    track(exchange, "analytics-service", path, 200, startNano);
                    return;
                }
                if (path.endsWith("/keywords")) {
                    String text = payload.getString("text", "");
                    int limit = (int) payload.getNumber("limit", 5);
                    KeywordResult result = analyticsService.keywords(text, limit);
                    Map<String, Object> response = new HashMap<>();
                    response.put("keywords", result.topKeywords());
                    response.put("frequency", result.frequency());
                    respondJson(exchange, 200, JsonUtil.toJsonObject(response));
                    track(exchange, "analytics-service", path, 200, startNano);
                    return;
                }
                respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                track(exchange, "analytics-service", path, 404, startNano);
            } catch (IllegalArgumentException ex) {
                respondJson(exchange, 400, "{\"error\":\"" + ex.getMessage() + "\"}");
                track(exchange, "analytics-service", path, 400, startNano);
            }
        }
    }

    private static class CloudHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long startNano = MetricsRegistry.nowMs();
            String path = exchange.getRequestURI().getPath();
            if ("/health".equals(path)) {
                respondJson(exchange, 200, healthPayload("cloud-service"));
                track(exchange, "cloud-service", path, 200, startNano);
                return;
            }
            if ("/metrics".equals(path)) {
                if (MetricsRegistry.isEnabled()) {
                    respondJson(exchange, 200, MetricsRegistry.snapshot());
                    track(exchange, "cloud-service", path, 200, startNano);
                } else {
                    respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                    track(exchange, "cloud-service", path, 404, startNano);
                }
                return;
            }
            if (!path.startsWith("/cloud")) {
                respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                track(exchange, "cloud-service", path, 404, startNano);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                respondJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                track(exchange, "cloud-service", path, 405, startNano);
                return;
            }
            try {
                JsonObject payload = parseJsonObject(exchange);
                if (path.endsWith("/allocate")) {
                    List<NodeCapacity> nodes = parseNodes(payload);
                    List<Workload> workloads = parseWorkloads(payload);
                    if (policyEnforcer.isEnabled()) {
                        PolicyDecision decision = policyEnforcer.evaluate(workloads);
                        if (!decision.allowed()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("risk", decision.riskLevel().name().toLowerCase());
                            response.put("reasons", decision.reasons());
                            respondJson(exchange, 403, JsonUtil.toJsonObject(response));
                            track(exchange, "cloud-service", path, 403, startNano);
                            return;
                        }
                    }
                    ClusterPlan plan = resourceAllocator.allocate(nodes, workloads);
                    Map<String, Object> response = new HashMap<>();
                    response.put("placements", plan.placements());
                    /*
                    response.put("remaining", plan.remainingCapacity().stream().map(node -> Map.of(\n                            \"nodeId\", node.nodeId(),\n                            \"cpu\", node.cpuCores(),\n                            \"memory\", node.memoryGb()\n                    )).toList());
                    */
                    response.put("remaining", plan.remainingCapacity().stream().map(node -> Map.of(
                            "nodeId", node.nodeId(),
                            "cpu", node.cpuCores(),
                            "memory", node.memoryGb()
                    )).toList());
                    respondJson(exchange, 200, JsonUtil.toJsonObject(response));
                    track(exchange, "cloud-service", path, 200, startNano);
                    return;
                }
                if (path.endsWith("/plan/kubernetes")) {
                    List<Workload> workloads = parseWorkloads(payload);
                    if (policyEnforcer.isEnabled()) {
                        PolicyDecision decision = policyEnforcer.evaluate(workloads);
                        if (!decision.allowed()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("risk", decision.riskLevel().name().toLowerCase());
                            response.put("reasons", decision.reasons());
                            respondJson(exchange, 403, JsonUtil.toJsonObject(response));
                            track(exchange, "cloud-service", path, 403, startNano);
                            return;
                        }
                    }
                    Map<String, Object> response = new HashMap<>();
                    response.put("manifests", kubernetesPlanner.planDeployments(workloads));
                    respondJson(exchange, 200, JsonUtil.toJsonObject(response));
                    track(exchange, "cloud-service", path, 200, startNano);
                    return;
                }
                if (path.endsWith("/plan/docker")) {
                    List<Workload> workloads = parseWorkloads(payload);
                    if (policyEnforcer.isEnabled()) {
                        PolicyDecision decision = policyEnforcer.evaluate(workloads);
                        if (!decision.allowed()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("risk", decision.riskLevel().name().toLowerCase());
                            response.put("reasons", decision.reasons());
                            respondJson(exchange, 403, JsonUtil.toJsonObject(response));
                            track(exchange, "cloud-service", path, 403, startNano);
                            return;
                        }
                    }
                    Map<String, Object> response = new HashMap<>();
                    response.put("commands", dockerOrchestrator.planContainers(workloads));
                    respondJson(exchange, 200, JsonUtil.toJsonObject(response));
                    track(exchange, "cloud-service", path, 200, startNano);
                    return;
                }
                respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                track(exchange, "cloud-service", path, 404, startNano);
            } catch (IllegalArgumentException ex) {
                respondJson(exchange, 400, "{\"error\":\"" + ex.getMessage() + "\"}");
                track(exchange, "cloud-service", path, 400, startNano);
            }
        }
    }

    private static void respondJson(HttpExchange exchange, int status, String body) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "application/json");
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static void track(HttpExchange exchange, String service, String path, int status, long startNano) {
        long duration = MetricsRegistry.elapsedMs(startNano);
        MetricsRegistry.increment(service + ".requests");
        MetricsRegistry.recordDuration(service + ".latency_ms", duration);
        StructuredLogger.request(service, path, exchange.getRequestMethod(), status, duration);
    }

    private static String healthPayload(String serviceName) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "ok");
        payload.put("service", serviceName);
        payload.put("started_at", startedAt.toString());
        payload.put("uptime_seconds", (Instant.now().toEpochMilli() - startedAt.toEpochMilli()) / 1000.0);
        payload.put("users", users.size());
        payload.put("payments", payments.size());
        payload.put("notifications", notifications.size());
        return JsonUtil.toJsonObject(payload);
    }

    private static JsonObject parseJsonObject(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
        JsonValue value = JsonParser.parse(body);
        if (value instanceof JsonObject object) {
            return object;
        }
        throw new IllegalArgumentException("Expected JSON object");
    }

    private static List<Double> parseNumberList(JsonObject payload, String key) {
        JsonValue value = payload.get(key);
        if (value == null || !(value instanceof JsonArray array)) {
            throw new IllegalArgumentException("Expected array field: " + key);
        }
        List<Double> numbers = new ArrayList<>();
        for (JsonValue item : array.asArray()) {
            numbers.add(item.asNumber());
        }
        return numbers;
    }

    private static List<NodeCapacity> parseNodes(JsonObject payload) {
        JsonValue value = payload.get("nodes");
        if (value == null || !(value instanceof JsonArray array)) {
            throw new IllegalArgumentException("Expected nodes array");
        }
        List<NodeCapacity> nodes = new ArrayList<>();
        for (JsonValue item : array.asArray()) {
            JsonObject node = (JsonObject) item;
            String id = node.getString("id", "");
            int cpu = (int) node.getNumber("cpu", 0);
            int memory = (int) node.getNumber("memory", 0);
            nodes.add(new NodeCapacity(id, cpu, memory));
        }
        return nodes;
    }

    private static List<Workload> parseWorkloads(JsonObject payload) {
        JsonValue value = payload.get("workloads");
        if (value == null || !(value instanceof JsonArray array)) {
            throw new IllegalArgumentException("Expected workloads array");
        }
        List<Workload> workloads = new ArrayList<>();
        for (JsonValue item : array.asArray()) {
            JsonObject workload = (JsonObject) item;
            String id = workload.getString("id", "");
            int cpu = (int) workload.getNumber("cpu", 0);
            int memory = (int) workload.getNumber("memory", 0);
            String tier = workload.getString("tier", "standard");
            workloads.add(new Workload(id, cpu, memory, tier));
        }
        return workloads;
    }

    private record User(int id, String name, String email) {}
    private record Payment(int id, double amount, int userId) {}
    private record Notification(int id, String message, int userId) {}

    private static class JsonWriter {
        static Map<String, String> parseBody(HttpExchange exchange) throws IOException {
            byte[] bytes = exchange.getRequestBody().readAllBytes();
            String body = new String(bytes, StandardCharsets.UTF_8).trim();
            Map<String, String> result = new HashMap<>();
            if (body.startsWith("{") && body.endsWith("}")) {
                body = body.substring(1, body.length() - 1);
            }
            if (!body.isEmpty()) {
                String[] parts = body.split(",");
                for (String part : parts) {
                    String[] kv = part.split(":", 2);
                    if (kv.length == 2) {
                        String key = stripQuotes(kv[0].trim());
                        String value = stripQuotes(kv[1].trim());
                        result.put(key, value);
                    }
                }
            }
            return result;
        }

        static String writeUsers(List<User> currentUsers) {
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < currentUsers.size(); i++) {
                builder.append(writeUser(currentUsers.get(i)));
                if (i < currentUsers.size() - 1) {
                    builder.append(',');
                }
            }
            builder.append(']');
            return builder.toString();
        }

        static String writePayments(List<Payment> currentPayments) {
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < currentPayments.size(); i++) {
                builder.append(writePayment(currentPayments.get(i)));
                if (i < currentPayments.size() - 1) {
                    builder.append(',');
                }
            }
            builder.append(']');
            return builder.toString();
        }

        static String writeNotifications(List<Notification> currentNotifications) {
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < currentNotifications.size(); i++) {
                builder.append(writeNotification(currentNotifications.get(i)));
                if (i < currentNotifications.size() - 1) {
                    builder.append(',');
                }
            }
            builder.append(']');
            return builder.toString();
        }

        static String writeUser(User user) {
            return String.format("{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\"}", user.id(), user.name(), user.email());
        }

        static String writePayment(Payment payment) {
            return String.format("{\"id\":%d,\"amount\":%s,\"userId\":%d}", payment.id(), Double.toString(payment.amount()), payment.userId());
        }

        static String writeNotification(Notification notification) {
            return String.format("{\"id\":%d,\"message\":\"%s\",\"userId\":%d}", notification.id(), notification.message(), notification.userId());
        }

        private static String stripQuotes(String value) {
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }
    }
}
