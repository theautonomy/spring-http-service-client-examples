package com.example.demo.config.restclient;

public class ClientAuthProperties {

    private Authentication authentication;
    private String apiVersionDefault; // Workaround for Spring Boot binding issue with

    // apiversion.defaultVersion

    public String getApiVersionDefault() {
        return apiVersionDefault;
    }

    public void setApiVersionDefault(String apiVersionDefault) {
        this.apiVersionDefault = apiVersionDefault;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public static class Authentication {
        private String type; // "basic", "oauth2", "bearer", or null (no auth)
        private BasicAuth basic;
        private OAuth2Auth oauth2;
        private BearerAuth bearer;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public BasicAuth getBasic() {
            return basic;
        }

        public void setBasic(BasicAuth basic) {
            this.basic = basic;
        }

        public OAuth2Auth getOauth2() {
            return oauth2;
        }

        public void setOauth2(OAuth2Auth oauth2) {
            this.oauth2 = oauth2;
        }

        public BearerAuth getBearer() {
            return bearer;
        }

        public void setBearer(BearerAuth bearer) {
            this.bearer = bearer;
        }
    }

    public static class BasicAuth {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class OAuth2Auth {
        private String registrationId;

        public String getRegistrationId() {
            return registrationId;
        }

        public void setRegistrationId(String registrationId) {
            this.registrationId = registrationId;
        }
    }

    public static class BearerAuth {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
