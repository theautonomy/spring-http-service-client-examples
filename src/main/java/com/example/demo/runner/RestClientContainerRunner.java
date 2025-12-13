package com.example.demo.runner;

import com.example.demo.config.restclient.RestClientContainer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestClientContainerRunner implements CommandLineRunner {

    private final RestClientContainer restClients;

    public RestClientContainerRunner(RestClientContainer restClients) {
        this.restClients = restClients;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== RestClientContainer Test ===\n");

        // Print all available service clients
        System.out.println("Available service clients: " + restClients.getNames());

        // Test 1: Use pre-built RestClient for jph (bearer auth)
        System.out.println("\n--- Test 1: JPH RestClient (bearer auth) ---");
        try {
            RestClient jph = restClients.get("jph");
            String posts = jph.get().uri("/posts/1").retrieve().body(String.class);
            System.out.println("JPH Response (truncated): " + truncate(posts, 200));
        } catch (Exception e) {
            System.out.println("JPH Error: " + e.getMessage());
        }

        // Test 2: Use pre-built RestClient for ara (no auth)
        System.out.println("\n--- Test 2: ARA RestClient (no auth) ---");
        try {
            RestClient ara = restClients.get("ara");
            String objects = ara.get().uri("/objects").retrieve().body(String.class);
            System.out.println("ARA Response (truncated): " + truncate(objects, 200));
        } catch (Exception e) {
            System.out.println("ARA Error: " + e.getMessage());
        }

        // Test 3: Use getBuilder() and customize further
        System.out.println("\n--- Test 3: JPH Builder with custom header ---");
        try {
            RestClient.Builder builder = restClients.getBuilder("jph");
            RestClient customClient =
                    builder.defaultHeader("X-Custom-Test", "from-builder").build();
            String posts = customClient.get().uri("/posts/2").retrieve().body(String.class);
            System.out.println("Custom JPH Response (truncated): " + truncate(posts, 200));
        } catch (Exception e) {
            System.out.println("Custom JPH Error: " + e.getMessage());
        }

        // Test 4: httpbin with basic auth (may fail if mock server not running)
        System.out.println("\n--- Test 4: HTTPBin RestClient (basic auth) ---");
        try {
            if (restClients.contains("httpbin")) {
                RestClient httpbin = restClients.get("httpbin");
                String response = httpbin.get().uri("/get").retrieve().body(String.class);
                System.out.println("HTTPBin Response (truncated): " + truncate(response, 200));
            } else {
                System.out.println("HTTPBin not configured");
            }
        } catch (Exception e) {
            System.out.println("HTTPBin Error: " + e.getMessage());
        }

        System.out.println("\n=== RestClientContainer Test Complete ===\n");
    }

    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "null";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}
