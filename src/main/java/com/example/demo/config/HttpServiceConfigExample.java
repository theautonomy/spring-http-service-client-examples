package com.example.demo.config;

import com.example.demo.client.httpbin.HttpBinClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpServiceConfigExample {

    // Configure http service client
    // This takes precedence and will be used
    // Spring won't configure this with @ImportHttpServices(group = "httpbin", basePackages =
    // "com.example.demo.client.httpbin")
    @Bean
    HttpBinClient httpBinClient(
            RestClient.Builder builder,
            @Value("${spring.http.serviceclient.httpbin.base-url}") String baseUrl) {
        builder.requestInterceptor(new LoggingInterceptor());
        var restClient = builder.baseUrl(baseUrl).build();

        var adapter = RestClientAdapter.create(restClient);
        var factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(HttpBinClient.class);
    }
}
