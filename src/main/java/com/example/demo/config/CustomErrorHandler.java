package com.example.demo.config;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

public class CustomErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response)
            throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();

        System.err.println("\n=== HTTP Error Detected ===");
        System.err.println("URL: " + url);
        System.err.println("Method: " + method);
        System.err.println("Status Code: " + statusCode);
        System.err.println("Status Text: " + response.getStatusText());
        System.err.println("Headers: " + response.getHeaders());
        System.err.println("===========================\n");

        if (statusCode.is4xxClientError()) {
            handleClientError(response, statusCode);
        } else if (statusCode.is5xxServerError()) {
            handleServerError(response, statusCode);
        } else {
            super.handleError(url, method, response);
        }
    }

    private void handleClientError(ClientHttpResponse response, HttpStatusCode statusCode)
            throws IOException {
        if (statusCode == HttpStatus.NOT_FOUND) {
            System.err.println("Resource not found (404). Please check the requested URL.");
        } else if (statusCode == HttpStatus.BAD_REQUEST) {
            System.err.println("Bad request (400). Please check the request parameters.");
        } else if (statusCode == HttpStatus.UNAUTHORIZED) {
            System.err.println("Unauthorized (401). Authentication required.");
        } else if (statusCode == HttpStatus.FORBIDDEN) {
            System.err.println("Forbidden (403). Access denied.");
        }
        throw new HttpClientErrorException(
                statusCode, response.getStatusText(), response.getHeaders(), null, null);
    }

    private void handleServerError(ClientHttpResponse response, HttpStatusCode statusCode)
            throws IOException {
        if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR) {
            System.err.println("Internal server error (500). The server encountered an error.");
        } else if (statusCode == HttpStatus.SERVICE_UNAVAILABLE) {
            System.err.println("Service unavailable (503). Please try again later.");
        } else if (statusCode == HttpStatus.GATEWAY_TIMEOUT) {
            System.err.println("Gateway timeout (504). The server took too long to respond.");
        }
        throw new HttpServerErrorException(
                statusCode, response.getStatusText(), response.getHeaders(), null, null);
    }
}
