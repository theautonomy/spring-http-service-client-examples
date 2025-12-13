package com.example.demo.config.restclient;

import java.util.HashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "serviceclient")
public class ServiceClientAuthProperties extends HashMap<String, ClientAuthProperties> {}
