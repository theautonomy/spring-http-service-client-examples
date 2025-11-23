package com.example.demo.config;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

/**
 * OAuth2 interceptor that adds Bearer token to outgoing requests.
 *
 * <p>This interceptor retrieves OAuth2 access tokens using OAuth2AuthorizedClientManager and adds
 * them as Authorization header to HTTP requests.
 */
public class OAuth2ClientInterceptor implements ClientHttpRequestInterceptor {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final String clientRegistrationId;

    public OAuth2ClientInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager, String clientRegistrationId) {
        this.authorizedClientManager = authorizedClientManager;
        this.clientRegistrationId = clientRegistrationId;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        // Get the authorized client (with access token)
        OAuth2AuthorizedClient authorizedClient = getAuthorizedClient();

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            // Add Bearer token to Authorization header
            String token = authorizedClient.getAccessToken().getTokenValue();
            request.getHeaders().setBearerAuth(token);
            System.out.println(
                    "[OAuth2Interceptor] Added Bearer token for: " + clientRegistrationId);
        } else {
            System.out.println(
                    "[OAuth2Interceptor] No token available for: " + clientRegistrationId);
        }

        return execution.execute(request, body);
    }

    private OAuth2AuthorizedClient getAuthorizedClient() {
        Authentication principal = createPrincipal();

        OAuth2AuthorizeRequest authorizeRequest =
                OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId)
                        .principal(principal)
                        .build();

        return authorizedClientManager.authorize(authorizeRequest);
    }

    private Authentication createPrincipal() {
        // For client credentials flow, create a simple principal
        // For authorization code flow, this would come from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            return authentication;
        }

        // Return anonymous authentication for client credentials
        return new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_ANONYMOUS")));
    }
}
