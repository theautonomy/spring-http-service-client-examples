package com.example.demo.controller;

import com.example.demo.client.github.GithubUserService;
import com.example.demo.model.GithubUser;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github")
public class GithubController {

    private final GithubUserService githubUserService;

    public GithubController(GithubUserService githubUserService) {
        this.githubUserService = githubUserService;
    }

    @GetMapping("/user")
    public GithubUser getAuthenticatedUser() {
        return githubUserService.getAuthenticatedUser();
    }
}
