package com.example.demo.model;

public record Comment(Long id, Long postId, String name, String email, String body) {}
