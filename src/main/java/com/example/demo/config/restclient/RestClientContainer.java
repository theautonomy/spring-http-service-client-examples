package com.example.demo.config.restclient;

import java.util.Set;

import org.springframework.web.client.RestClient;

public interface RestClientContainer {

    /**
     * Get a RestClient by its service client name.
     *
     * @param name the service client name (e.g., "github", "httpbin")
     * @return the configured RestClient
     * @throws IllegalArgumentException if no RestClient exists with the given name
     */
    RestClient get(String name);

    /**
     * Get a pre-configured RestClient.Builder by its service client name. The builder has all
     * configuration applied (base-url, headers, timeouts, authentication) but is not yet built,
     * allowing further customization.
     *
     * @param name the service client name (e.g., "github", "httpbin")
     * @return a new RestClient.Builder with all configuration applied
     * @throws IllegalArgumentException if no configuration exists with the given name
     */
    RestClient.Builder getBuilder(String name);

    /**
     * Check if a RestClient with the given name exists.
     *
     * @param name the service client name
     * @return true if a RestClient exists with the given name
     */
    boolean contains(String name);

    /**
     * Get all available service client names.
     *
     * @return set of service client names
     */
    Set<String> getNames();
}
