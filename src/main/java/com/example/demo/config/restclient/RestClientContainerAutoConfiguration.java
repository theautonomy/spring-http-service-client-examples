package com.example.demo.config.restclient;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.autoconfigure.HttpClientProperties;
import org.springframework.boot.http.client.autoconfigure.service.HttpServiceClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ServiceClientAuthProperties.class)
public class RestClientContainerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestClientContainer restClientContainer(
            RestClient.Builder restClientBuilder,
            ClientHttpRequestFactoryBuilder requestFactoryBuilder,
            HttpServiceClientProperties httpServiceClientProperties,
            ServiceClientAuthProperties authProperties,
            @Nullable OAuth2AuthorizedClientManager authorizedClientManager) {

        DefaultRestClientContainer container = new DefaultRestClientContainer();

        // Iterate over each service client defined in spring.http.serviceclient.*
        httpServiceClientProperties.forEach(
                (name, clientProps) -> {
                    RestClient client =
                            buildRestClient(
                                    name,
                                    restClientBuilder.clone(),
                                    requestFactoryBuilder,
                                    clientProps,
                                    authProperties.get(name),
                                    authorizedClientManager);
                    container.register(name, client);
                });

        return container;
    }

    private RestClient buildRestClient(
            String name,
            RestClient.Builder builder,
            ClientHttpRequestFactoryBuilder requestFactoryBuilder,
            HttpClientProperties clientProps,
            @Nullable ClientAuthProperties authProps,
            @Nullable OAuth2AuthorizedClientManager authorizedClientManager) {

        // 1. Set base URL
        if (clientProps.getBaseUrl() != null) {
            builder.baseUrl(clientProps.getBaseUrl());
        }

        // 2. Add default headers
        if (clientProps.getDefaultHeader() != null && !clientProps.getDefaultHeader().isEmpty()) {
            builder.defaultHeaders(
                    headers -> {
                        clientProps
                                .getDefaultHeader()
                                .forEach(
                                        (headerName, values) -> {
                                            values.forEach(value -> headers.add(headerName, value));
                                        });
                    });
        }

        // 3. Add API version header if configured
        if (clientProps.getApiversion() != null) {
            var apiversion = clientProps.getApiversion();
            if (apiversion.getDefaultVersion() != null && apiversion.getInsert() != null) {
                var insert = apiversion.getInsert();
                if (insert.getHeader() != null) {
                    builder.defaultHeaders(
                            headers -> {
                                headers.add(insert.getHeader(), apiversion.getDefaultVersion());
                            });
                }
                // TODO: Support other API version insert types (query-param, path-segment,
                // media-type)
            }
        }

        // 4. Build ClientHttpRequestFactory with timeouts
        HttpClientSettings settings = buildHttpClientSettings(clientProps);
        if (settings != null) {
            builder.requestFactory(requestFactoryBuilder.build(settings));
        }

        // 5. Add authentication
        configureAuthentication(name, builder, authProps, authorizedClientManager);

        return builder.build();
    }

    @Nullable
    private HttpClientSettings buildHttpClientSettings(HttpClientProperties clientProps) {
        Duration connectTimeout = clientProps.getConnectTimeout();
        Duration readTimeout = clientProps.getReadTimeout();

        if (connectTimeout == null && readTimeout == null) {
            return null;
        }

        HttpClientSettings settings = HttpClientSettings.defaults();
        if (connectTimeout != null) {
            settings = settings.withConnectTimeout(connectTimeout);
        }
        if (readTimeout != null) {
            settings = settings.withReadTimeout(readTimeout);
        }
        return settings;
    }

    private void configureAuthentication(
            String name,
            RestClient.Builder builder,
            @Nullable ClientAuthProperties authProps,
            @Nullable OAuth2AuthorizedClientManager authorizedClientManager) {

        if (authProps == null || authProps.getAuthentication() == null) {
            return;
        }

        String authType = authProps.getAuthentication();

        switch (authType.toLowerCase()) {
            case "basic" -> configureBasicAuth(builder, authProps);
            case "oauth2" -> configureOAuth2Auth(name, builder, authProps, authorizedClientManager);
            case "bearer" -> configureBearerAuth(builder, authProps);
            default -> {
                // No authentication or unknown type
            }
        }
    }

    private void configureBasicAuth(RestClient.Builder builder, ClientAuthProperties authProps) {
        var authDetails = authProps.getAuthenticationDetails();
        if (authDetails == null || authDetails.getBasic() == null) {
            return;
        }

        var basic = authDetails.getBasic();
        if (basic.getUsername() != null && basic.getPassword() != null) {
            builder.defaultHeaders(
                    headers -> {
                        headers.setBasicAuth(basic.getUsername(), basic.getPassword());
                    });
        }
    }

    private void configureOAuth2Auth(
            String name,
            RestClient.Builder builder,
            ClientAuthProperties authProps,
            @Nullable OAuth2AuthorizedClientManager authorizedClientManager) {

        if (authorizedClientManager == null) {
            throw new IllegalStateException(
                    "OAuth2AuthorizedClientManager is required for OAuth2 authentication on service client: "
                            + name);
        }

        var authDetails = authProps.getAuthenticationDetails();
        if (authDetails == null || authDetails.getOauth2() == null) {
            return;
        }

        var oauth2 = authDetails.getOauth2();
        String registrationId = oauth2.getRegistrationId();
        if (registrationId == null) {
            registrationId = name; // Default to service client name
        }

        final String finalRegistrationId = registrationId;
        var oauth2Interceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        oauth2Interceptor.setClientRegistrationIdResolver(request -> finalRegistrationId);
        builder.requestInterceptor(oauth2Interceptor);
    }

    private void configureBearerAuth(RestClient.Builder builder, ClientAuthProperties authProps) {
        var authDetails = authProps.getAuthenticationDetails();
        if (authDetails == null || authDetails.getBearer() == null) {
            return;
        }

        var bearer = authDetails.getBearer();
        if (bearer.getToken() != null) {
            builder.defaultHeaders(
                    headers -> {
                        headers.setBearerAuth(bearer.getToken());
                    });
        }
    }
}
