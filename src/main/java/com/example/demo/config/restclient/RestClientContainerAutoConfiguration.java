package com.example.demo.config.restclient;

import java.time.Duration;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.autoconfigure.ApiversionProperties;
import org.springframework.boot.http.client.autoconfigure.HttpClientProperties;
import org.springframework.boot.http.client.autoconfigure.service.HttpServiceClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ServiceClientAuthProperties.class)
public class RestClientContainerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestClientContainer restClientContainer(
            RestClient.Builder restClientBuilder,
            ClientHttpRequestFactoryBuilder<?> requestFactoryBuilder,
            HttpServiceClientProperties httpServiceClientProperties,
            ServiceClientAuthProperties authProperties,
            @Nullable OAuth2AuthorizedClientManager authorizedClientManager) {

        DefaultRestClientContainer container = new DefaultRestClientContainer();

        // Iterate over each service client defined in spring.http.serviceclient.*
        httpServiceClientProperties.forEach(
                (name, clientProps) -> {
                    ClientAuthProperties authProps = authProperties.get(name);

                    // Register builder supplier (creates a fresh configured builder each time)
                    container.registerBuilderSupplier(
                            name,
                            () ->
                                    configureBuilder(
                                            name,
                                            restClientBuilder.clone(),
                                            requestFactoryBuilder,
                                            clientProps,
                                            authProps,
                                            authorizedClientManager));

                    // Register pre-built RestClient
                    RestClient client =
                            configureBuilder(
                                            name,
                                            restClientBuilder.clone(),
                                            requestFactoryBuilder,
                                            clientProps,
                                            authProps,
                                            authorizedClientManager)
                                    .build();
                    container.register(name, client);
                });

        return container;
    }

    private RestClient.Builder configureBuilder(
            String name,
            RestClient.Builder builder,
            ClientHttpRequestFactoryBuilder<?> requestFactoryBuilder,
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

        // 3. Configure API versioning using ApiVersionInserter
        configureApiVersion(builder, clientProps, authProps);

        // 4. Build ClientHttpRequestFactory with timeouts
        HttpClientSettings settings = buildHttpClientSettings(clientProps);
        if (settings != null) {
            builder.requestFactory(requestFactoryBuilder.build(settings));
        }

        // 5. Add authentication
        configureAuthentication(name, builder, authProps, authorizedClientManager);

        return builder;
    }

    private void configureApiVersion(
            RestClient.Builder builder,
            HttpClientProperties clientProps,
            @Nullable ClientAuthProperties authProps) {
        if (clientProps.getApiversion() == null) {
            return;
        }

        var apiversion = clientProps.getApiversion();
        var insert = apiversion.getInsert();

        if (insert == null) {
            return;
        }

        // Get default version from our custom properties (workaround for Spring Boot binding issue)
        String defaultVersion = (authProps != null) ? authProps.getApiVersionDefault() : null;

        if (defaultVersion == null) {
            return;
        }

        // Set the default version
        builder.defaultApiVersion(defaultVersion);

        // Create the appropriate ApiVersionInserter based on insert type
        ApiVersionInserter inserter = createApiVersionInserter(insert);
        if (inserter != null) {
            builder.apiVersionInserter(inserter);
        }
    }

    @Nullable
    private ApiVersionInserter createApiVersionInserter(ApiversionProperties.Insert insert) {
        // Header-based: X-API-VERSION: 1.0
        if (insert.getHeader() != null) {
            return ApiVersionInserter.useHeader(insert.getHeader());
        }

        // Query parameter-based: ?version=1.0
        if (insert.getQueryParameter() != null) {
            return ApiVersionInserter.useQueryParam(insert.getQueryParameter());
        }

        // Path segment-based: /api/v1.0/users
        if (insert.getPathSegment() != null) {
            return ApiVersionInserter.usePathSegment(insert.getPathSegment());
        }

        // Media type parameter-based: Accept: application/json;version=1.0
        if (insert.getMediaTypeParameter() != null) {
            return ApiVersionInserter.useMediaTypeParam(insert.getMediaTypeParameter());
        }

        return null;
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

        var auth = authProps.getAuthentication();
        if (auth.getType() == null) {
            return;
        }

        switch (auth.getType().toLowerCase()) {
            case "basic" -> configureBasicAuth(builder, auth);
            case "oauth2" -> configureOAuth2Auth(name, builder, auth, authorizedClientManager);
            case "bearer" -> configureBearerAuth(builder, auth);
            default -> {
                // No authentication or unknown type
            }
        }
    }

    private void configureBasicAuth(
            RestClient.Builder builder, ClientAuthProperties.Authentication auth) {
        var basic = auth.getBasic();
        if (basic == null) {
            return;
        }

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
            ClientAuthProperties.Authentication auth,
            @Nullable OAuth2AuthorizedClientManager authorizedClientManager) {

        if (authorizedClientManager == null) {
            throw new IllegalStateException(
                    "OAuth2AuthorizedClientManager is required for OAuth2 authentication on service client: "
                            + name);
        }

        var oauth2 = auth.getOauth2();
        if (oauth2 == null) {
            return;
        }

        String registrationId = oauth2.getRegistrationId();
        if (registrationId == null) {
            registrationId = name; // Default to service client name
        }

        final String finalRegistrationId = registrationId;
        var oauth2Interceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        oauth2Interceptor.setClientRegistrationIdResolver(request -> finalRegistrationId);
        builder.requestInterceptor(oauth2Interceptor);
    }

    private void configureBearerAuth(
            RestClient.Builder builder, ClientAuthProperties.Authentication auth) {
        var bearer = auth.getBearer();
        if (bearer == null) {
            return;
        }

        if (bearer.getToken() != null) {
            builder.defaultHeaders(
                    headers -> {
                        headers.setBearerAuth(bearer.getToken());
                    });
        }
    }
}
