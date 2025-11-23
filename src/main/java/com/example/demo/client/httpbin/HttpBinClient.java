package com.example.demo.client.httpbin;

import com.example.demo.model.BasicAuthResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface HttpBinClient {

    @GetExchange("/basic-auth/{user}/{password}")
    BasicAuthResponse testBasicAuth(@PathVariable String user, @PathVariable String password);
}
