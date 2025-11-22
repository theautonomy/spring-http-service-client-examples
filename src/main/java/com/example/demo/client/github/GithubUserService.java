package com.example.demo.client.github;

import com.example.demo.model.GithubUser;

import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/user")
public interface GithubUserService {

    @ClientRegistrationId("github")
    @GetExchange
    GithubUser getAuthenticatedUser();
}
