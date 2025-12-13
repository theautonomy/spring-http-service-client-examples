package com.example.demo.config.restclient;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.client.RestClient;

public class DefaultRestClientContainer implements RestClientContainer {

    private final Map<String, RestClient> clients = new ConcurrentHashMap<>();

    @Override
    public RestClient get(String name) {
        RestClient client = clients.get(name);
        if (client == null) {
            throw new IllegalArgumentException("No RestClient found with name: " + name);
        }
        return client;
    }

    @Override
    public boolean contains(String name) {
        return clients.containsKey(name);
    }

    @Override
    public Set<String> getNames() {
        return Collections.unmodifiableSet(clients.keySet());
    }

    /**
     * Register a RestClient with the given name.
     *
     * @param name the service client name
     * @param client the RestClient instance
     */
    public void register(String name, RestClient client) {
        clients.put(name, client);
    }
}
