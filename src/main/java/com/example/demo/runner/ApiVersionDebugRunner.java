package com.example.demo.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.http.client.autoconfigure.service.HttpServiceClientProperties;
import org.springframework.stereotype.Component;

@Component
public class ApiVersionDebugRunner implements ApplicationRunner {

    private final HttpServiceClientProperties httpServiceClientProperties;

    public ApiVersionDebugRunner(HttpServiceClientProperties httpServiceClientProperties) {
        this.httpServiceClientProperties = httpServiceClientProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("\n=== API Version Configuration Debug ===\n");

        httpServiceClientProperties.forEach(
                (name, props) -> {
                    System.out.println("Service Client: " + name);
                    System.out.println("  Base URL: " + props.getBaseUrl());

                    var apiversion = props.getApiversion();
                    if (apiversion == null) {
                        System.out.println("  API Version: NOT CONFIGURED");
                    } else {
                        System.out.println("  API Version:");
                        System.out.println(
                                "    getDefaultVersion(): " + apiversion.getDefaultVersion());

                        // Print all fields
                        System.out.println("    --- Fields ---");
                        for (var field : apiversion.getClass().getDeclaredFields()) {
                            field.setAccessible(true);
                            try {
                                System.out.println(
                                        "    Field '"
                                                + field.getName()
                                                + "': "
                                                + field.get(apiversion));
                            } catch (Exception e) {
                                System.out.println(
                                        "    Field '"
                                                + field.getName()
                                                + "': ERROR - "
                                                + e.getMessage());
                            }
                        }

                        // Print setter methods
                        System.out.println("    --- Setters ---");
                        for (var method : apiversion.getClass().getMethods()) {
                            if (method.getName().startsWith("set")) {
                                System.out.println(
                                        "    "
                                                + method.getName()
                                                + "("
                                                + (method.getParameterCount() > 0
                                                        ? method.getParameterTypes()[0]
                                                                .getSimpleName()
                                                        : "")
                                                + ")");
                            }
                        }

                        var insert = apiversion.getInsert();
                        if (insert == null) {
                            System.out.println("    Insert: NULL");
                        } else {
                            System.out.println("    Insert.Header: " + insert.getHeader());
                            System.out.println(
                                    "    Insert.QueryParameter: " + insert.getQueryParameter());
                            System.out.println(
                                    "    Insert.PathSegment: " + insert.getPathSegment());
                            System.out.println(
                                    "    Insert.MediaTypeParameter: "
                                            + insert.getMediaTypeParameter());
                        }
                    }
                    System.out.println();
                });

        System.out.println("=== End API Version Debug ===\n");
    }
}
