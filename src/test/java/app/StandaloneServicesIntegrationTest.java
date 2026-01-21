package app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandaloneServicesIntegrationTest {
    private StandaloneServices.ServiceBundle bundle;
    private StandaloneServices.ServicePorts ports;
    private HttpClient client;

    @BeforeEach
    void setUp() throws Exception {
        bundle = StandaloneServices.startServers(new StandaloneServices.ServicePorts(0, 0, 0, 0, 0, 0));
        ports = bundle.resolvedPorts();
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    @AfterEach
    void tearDown() {
        bundle.stopAll();
    }

    @Test
    void userFlowAndAnalyticsAreAvailable() throws Exception {
        HttpResponse<String> createUser = postJson("http://localhost:" + ports.userPort() + "/users",
                "{\"name\":\"Test\",\"email\":\"test@example.com\"}");
        assertEquals(201, createUser.statusCode());

        HttpResponse<String> forecast = postJson("http://localhost:" + ports.analyticsPort() + "/analytics/forecast",
                "{\"series\":[1,2,3],\"horizon\":2}");
        assertEquals(200, forecast.statusCode());
        assertTrue(forecast.body().contains("forecast"));

        HttpResponse<String> allocation = postJson("http://localhost:" + ports.cloudPort() + "/cloud/allocate",
                "{\"nodes\":[{\"id\":\"n1\",\"cpu\":4,\"memory\":8}]," +
                        "\"workloads\":[{\"id\":\"w1\",\"cpu\":2,\"memory\":4,\"tier\":\"standard\"}]}"
        );
        assertEquals(200, allocation.statusCode());
        assertTrue(allocation.body().contains("placements"));

        HttpResponse<String> health = get("http://localhost:" + ports.userPort() + "/health");
        assertEquals(200, health.statusCode());
        assertTrue(health.body().contains("status"));
    }

    private HttpResponse<String> postJson(String url, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
