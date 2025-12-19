package app;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final AtomicInteger userIds = new AtomicInteger(0);
    private static final AtomicInteger paymentIds = new AtomicInteger(0);
    private static final AtomicInteger notificationIds = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        startServer(8080, new UserHandler());
        startServer(8081, new AuthHandler());
        startServer(8082, new PaymentHandler());
        startServer(8083, new NotificationHandler());

        System.out.println("User Service running at http://localhost:8080/users");
        System.out.println("Auth Service running at http://localhost:8081/login");
        System.out.println("Payment Service running at http://localhost:8082/payments");
        System.out.println("Notification Service running at http://localhost:8083/notifications");

        new CountDownLatch(1).await();
    }

    private static void startServer(int port, HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", handler);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    private static class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/users")) {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    respondJson(exchange, 200, JsonWriter.writeUsers(users));
                } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    Map<String, String> payload = JsonWriter.parseBody(exchange);
                    String name = payload.get("name");
                    String email = payload.get("email");
                    if (name == null || email == null) {
                        respondJson(exchange, 400, "{\"error\":\"name and email are required\"}");
                        return;
                    }
                    User created = new User(userIds.incrementAndGet(), name, email);
                    users.add(created);
                    respondJson(exchange, 201, JsonWriter.writeUser(created));
                } else if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String[] parts = path.split("/");
                    if (parts.length < 3) {
                        respondJson(exchange, 400, "{\"error\":\"Invalid user id\"}");
                        return;
                    }
                    try {
                        int id = Integer.parseInt(parts[2]);
                        boolean removed = users.removeIf(user -> user.id == id);
                        if (removed) {
                            respondJson(exchange, 204, "");
                        } else {
                            respondJson(exchange, 404, "{\"error\":\"User not found\"}");
                        }
                    } catch (NumberFormatException ex) {
                        respondJson(exchange, 400, "{\"error\":\"Invalid user id\"}");
                    }
                } else {
                    respondJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } else {
                respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
            }
        }
    }

    private static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"/login".equals(exchange.getRequestURI().getPath())) {
                respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                respondJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> payload = JsonWriter.parseBody(exchange);
            String username = payload.get("username");
            String password = payload.get("password");
            if (username != null && password != null && password.equals(validUsers.get(username))) {
                respondJson(exchange, 200, String.format("{\"token\":\"token_for_%s\"}", username));
            } else {
                respondJson(exchange, 401, "{\"error\":\"Invalid credentials\"}");
            }
        }
    }

    private static class PaymentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (!path.startsWith("/payments")) {
                respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                return;
            }
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                respondJson(exchange, 200, JsonWriter.writePayments(payments));
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
                } catch (NumberFormatException ex) {
                    respondJson(exchange, 400, "{\"error\":\"Invalid payment payload\"}");
                }
                return;
            }
            respondJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }
    }

    private static class NotificationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (!path.startsWith("/notifications")) {
                respondJson(exchange, 404, "{\"error\":\"Not Found\"}");
                return;
            }
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                respondJson(exchange, 200, JsonWriter.writeNotifications(notifications));
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> payload = JsonWriter.parseBody(exchange);
                String message = payload.get("message");
                try {
                    int userId = Integer.parseInt(payload.getOrDefault("userId", "0"));
                    if (message == null) {
                        respondJson(exchange, 400, "{\"error\":\"Message is required\"}");
                        return;
                    }
                    Notification notification = new Notification(notificationIds.incrementAndGet(), message, userId);
                    notifications.add(notification);
                    respondJson(exchange, 201, JsonWriter.writeNotification(notification));
                } catch (NumberFormatException ex) {
                    respondJson(exchange, 400, "{\"error\":\"Invalid notification payload\"}");
                }
                return;
            }
            respondJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
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
