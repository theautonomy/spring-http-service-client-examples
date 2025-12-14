package com.example.demo.controller;

import com.example.demo.client.github.GithubUserService;
import com.example.demo.client.otc.SecondGithubUserService;
import com.example.demo.config.restclient.RestClientContainer;
import com.example.demo.model.GithubUser;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github")
public class GithubController {

    private final GithubUserService githubUserService;
    private final SecondGithubUserService secondGithubUserService;
    private final RestClientContainer restClientContainer;

    public GithubController(
            GithubUserService githubUserService,
            SecondGithubUserService secondGithubUserService,
            RestClientContainer restClientContainer) {
        this.githubUserService = githubUserService;
        this.secondGithubUserService = secondGithubUserService;
        this.restClientContainer = restClientContainer;
    }

    @GetMapping("/user")
    public GithubUser getAuthenticatedUser() {
        return githubUserService.getAuthenticatedUser();
    }

    @GetMapping("/second-user")
    public GithubUser getSecondAuthenticatedUser() {
        return secondGithubUserService.getAuthenticatedUser();
    }

    @GetMapping("/third-user")
    public GithubUser getThirdAuthenticatedUser() {
        return restClientContainer
                .getHttpExchangeClient("github", GithubUserService.class)
                .getAuthenticatedUser();
    }
}
