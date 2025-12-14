package com.example.demo.config.restclient;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public class DefaultRestClientContainer implements RestClientContainer {

    private final Map<String, RestClient> clients = new ConcurrentHashMap<>();
    private final Map<String, Supplier<RestClient.Builder>> builderSuppliers =
            new ConcurrentHashMap<>();

    @Override
    public RestClient get(String name) {
        RestClient client = clients.get(name);
        if (client == null) {
            throw new IllegalArgumentException("No RestClient found with name: " + name);
        }
        return client;
    }

    @Override
    public RestClient.Builder getBuilder(String name) {
        Supplier<RestClient.Builder> supplier = builderSuppliers.get(name);
        if (supplier == null) {
            throw new IllegalArgumentException("No RestClient.Builder found with name: " + name);
        }
        return supplier.get();
    }

    @Override
    public <T> T getHttpExchangeClient(String name, Class<T> exchangeClientClass) {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(get(name)))
                .build()
                .createClient(exchangeClientClass);
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

    /**
     * Register a builder supplier with the given name. The supplier creates a new pre-configured
     * RestClient.Builder each time it's called.
     *
     * @param name the service client name
     * @param builderSupplier supplier that creates configured RestClient.Builder instances
     */
    public void registerBuilderSupplier(String name, Supplier<RestClient.Builder> builderSupplier) {
        builderSuppliers.put(name, builderSupplier);
    }
}
