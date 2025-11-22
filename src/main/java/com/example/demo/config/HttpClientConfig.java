package com.example.demo.config;

import com.example.demo.client.JsonPlaceholderClient;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(
        group = "jph",
        types = {JsonPlaceholderClient.class})
public class HttpClientConfig {}
