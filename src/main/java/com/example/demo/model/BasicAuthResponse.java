package com.example.demo.model;

/** Response model for HTTP Basic Authentication test endpoint. */
public record BasicAuthResponse(boolean authenticated, String user) {}
