package app;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class DemoRunner {
    private DemoRunner() {}

    public static void run() throws Exception {
        StandaloneServices.ServicePorts ports = new StandaloneServices.ServicePorts(0, 0, 0, 0, 0, 0);
        StandaloneServices.ServiceBundle bundle = StandaloneServices.startServers(ports);
        StandaloneServices.ServicePorts resolved = bundle.resolvedPorts();
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        boolean ok = true;
        try {
            ok &= postJson(client, "http://localhost:" + resolved.userPort() + "/users",
                    "{\"name\":\"Demo User\",\"email\":\"demo@example.com\"}");
            ok &= postJson(client, "http://localhost:" + resolved.authPort() + "/login",
                    "{\"username\":\"user1\",\"password\":\"password1\"}");
            ok &= postJson(client, "http://localhost:" + resolved.paymentPort() + "/payments",
                    "{\"amount\":42.0,\"userId\":1}");
            ok &= postJson(client, "http://localhost:" + resolved.notificationPort() + "/notifications",
                    "{\"message\":\"Welcome!\",\"userId\":1}");
            ok &= postJson(client, "http://localhost:" + resolved.analyticsPort() + "/analytics/forecast",
                    "{\"series\":[1,2,3,4],\"horizon\":2}");
            ok &= postJson(client, "http://localhost:" + resolved.cloudPort() + "/cloud/allocate",
                    "{\"nodes\":[{\"id\":\"node-a\",\"cpu\":8,\"memory\":16}]," +
                            "\"workloads\":[{\"id\":\"svc\",\"cpu\":2,\"memory\":4,\"tier\":\"standard\"}]}"
            );
        } finally {
            bundle.stopAll();
        }
        if (ok) {
            System.out.println("DEMO PASS: core services and analytics responded successfully.");
        } else {
            System.out.println("DEMO FAIL: one or more endpoints returned non-2xx.");
            System.exit(1);
        }
    }

    private static boolean postJson(HttpClient client, String url, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }
}
