package com.example.demo.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.example.demo.config.restclient.RestClientContainer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/restclient-container")
public class RestClientContainerController {

    private final RestClientContainer restClients;

    public RestClientContainerController(RestClientContainer restClients) {
        this.restClients = restClients;
    }

    @GetMapping("/names")
    public Map<String, Object> getNames() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("availableClients", restClients.getNames());
        return result;
    }

    @GetMapping("/test")
    public Map<String, Object> testAll() {
        Map<String, Object> results = new LinkedHashMap<>();
        results.put("availableClients", restClients.getNames());

        // Test JPH (bearer auth)
        results.put("jph", testClient("jph", "/posts/1"));

        // Test ARA (no auth)
        results.put("ara", testClient("ara", "/objects"));

        // Test HTTPBin (basic auth)
        results.put("httpbin", testClient("httpbin", "/get"));

        return results;
    }

    @GetMapping("/test/{name}")
    public Map<String, Object> testClient(@PathVariable String name) {
        return testClient(name, "/");
    }

    @GetMapping("/test/{name}/{*path}")
    public Map<String, Object> testClientWithPath(
            @PathVariable String name, @PathVariable String path) {
        return testClient(name, path);
    }

    private Map<String, Object> testClient(String name, String path) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("client", name);
        result.put("path", path);

        try {
            if (!restClients.contains(name)) {
                result.put("status", "error");
                result.put("message", "Client not found: " + name);
                return result;
            }

            RestClient client = restClients.get(name);
            String response = client.get().uri(path).retrieve().body(String.class);

            result.put("status", "success");
            result.put("response", truncate(response, 500));
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    @GetMapping("/test-builder/{name}")
    public Map<String, Object> testBuilder(@PathVariable String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("client", name);
        result.put("method", "getBuilder()");

        try {
            if (!restClients.contains(name)) {
                result.put("status", "error");
                result.put("message", "Client not found: " + name);
                return result;
            }

            // Get builder and add custom header
            RestClient.Builder builder = restClients.getBuilder(name);
            RestClient customClient =
                    builder.defaultHeader("X-Custom-Test", "from-builder-endpoint").build();

            String response = customClient.get().uri("/").retrieve().body(String.class);

            result.put("status", "success");
            result.put("customHeader", "X-Custom-Test: from-builder-endpoint");
            result.put("response", truncate(response, 500));
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }

    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}
