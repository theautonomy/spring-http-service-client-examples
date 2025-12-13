package com.example.demo.runner;

import java.util.List;
import java.util.Map;

import com.example.demo.client.ara.RestfulApiClient;
import com.example.demo.model.ApiObject;
import com.example.demo.model.ApiObjectRequest;

import org.springframework.boot.CommandLineRunner;

// @Component
public class RestfulApiRunner implements CommandLineRunner {

    private final RestfulApiClient restfulApiClient;

    public RestfulApiRunner(RestfulApiClient restfulApiClient) {
        this.restfulApiClient = restfulApiClient;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== Testing Restful-API.dev HTTP Service Client ===\n");

        // Test 1: Get all objects (limited to first 5)
        System.out.println("1. Fetching all objects (first 5):");
        List<ApiObject> objects = restfulApiClient.getAllObjects();
        objects.stream()
                .limit(5)
                .forEach(obj -> System.out.println("   - Object #" + obj.id() + ": " + obj.name()));

        // Test 2: Get a single object
        System.out.println("\n2. Fetching object with ID 7:");
        ApiObject object = restfulApiClient.getObjectById("7");
        System.out.println("   ID: " + object.id());
        System.out.println("   Name: " + object.name());
        System.out.println("   Data: " + object.data());

        // Test 3: Get objects by IDs
        System.out.println("\n3. Fetching objects by IDs (3, 5, 10):");
        List<ApiObject> objectsByIds = restfulApiClient.getObjectsByIds(List.of("3", "5", "10"));
        objectsByIds.forEach(
                obj -> System.out.println("   - Object #" + obj.id() + ": " + obj.name()));

        // Test 4: Create a new object
        System.out.println("\n4. Creating a new object:");
        ApiObjectRequest newObjectRequest =
                new ApiObjectRequest(
                        "Apple MacBook Pro 16",
                        Map.of(
                                "year",
                                2023,
                                "price",
                                2499.99,
                                "CPU model",
                                "M3 Max",
                                "Hard disk size",
                                "1 TB"));
        ApiObject createdObject = restfulApiClient.createObject(newObjectRequest);
        System.out.println("   Created object with ID: " + createdObject.id());
        System.out.println("   Name: " + createdObject.name());
        System.out.println("   Data: " + createdObject.data());

        // Test 5: Update an object
        System.out.println("\n5. Updating object with PUT:");
        ApiObjectRequest updateRequest =
                new ApiObjectRequest(
                        "Apple MacBook Pro 16 (Updated)",
                        Map.of("year", 2024, "price", 2599.99, "color", "Space Gray"));
        ApiObject updatedObject = restfulApiClient.updateObject(createdObject.id(), updateRequest);
        System.out.println("   Updated name: " + updatedObject.name());
        System.out.println("   Updated data: " + updatedObject.data());

        // Test 6: Partial update with PATCH
        System.out.println("\n6. Partial update with PATCH:");
        ApiObjectRequest patchRequest =
                new ApiObjectRequest(null, Map.of("price", 2399.99, "on_sale", true));
        ApiObject patchedObject =
                restfulApiClient.partialUpdateObject(createdObject.id(), patchRequest);
        System.out.println("   Patched data: " + patchedObject.data());

        // Test 7: Delete an object
        System.out.println("\n7. Deleting the created object:");
        restfulApiClient.deleteObject(createdObject.id());
        System.out.println("   Object deleted successfully");

        System.out.println("\n=== All Restful-API.dev tests completed! ===\n");
    }
}
