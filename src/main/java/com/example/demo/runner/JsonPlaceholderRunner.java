package com.example.demo.runner;

import java.util.List;

import com.example.demo.client.jph.JsonPlaceholderClient;
import com.example.demo.model.Comment;
import com.example.demo.model.Post;
import com.example.demo.model.User;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JsonPlaceholderRunner implements CommandLineRunner {

    private final JsonPlaceholderClient jsonPlaceholderClient;

    public JsonPlaceholderRunner(JsonPlaceholderClient jsonPlaceholderClient) {
        this.jsonPlaceholderClient = jsonPlaceholderClient;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== Testing JSONPlaceholder HTTP Service Client ===\n");

        // Test 1: Get all posts (limited to first 5)
        System.out.println("1. Fetching all posts (first 5):");
        List<Post> posts = jsonPlaceholderClient.getAllPosts();
        posts.stream()
                .limit(5)
                .forEach(
                        post ->
                                System.out.println(
                                        "   - Post #" + post.id() + ": " + post.title()));

        // Test 2: Get a single post
        System.out.println("\n2. Fetching post with ID 1:");
        Post post = jsonPlaceholderClient.getPostById(1L);
        System.out.println("   Title: " + post.title());
        System.out.println(
                "   Body: " + post.body().substring(0, Math.min(50, post.body().length())) + "...");

        // Test 3: Get comments for a post
        System.out.println("\n3. Fetching comments for post #1:");
        List<Comment> comments = jsonPlaceholderClient.getCommentsByPostId(1L);
        comments.stream()
                .limit(3)
                .forEach(
                        comment ->
                                System.out.println(
                                        "   - " + comment.name() + " by " + comment.email()));

        // Test 4: Get all users
        System.out.println("\n4. Fetching all users:");
        List<User> users = jsonPlaceholderClient.getAllUsers();
        users.forEach(
                user -> System.out.println("   - " + user.name() + " (@" + user.username() + ")"));

        // Test 5: Get a single user
        System.out.println("\n5. Fetching user with ID 1:");
        User user = jsonPlaceholderClient.getUserById(1L);
        System.out.println("   Name: " + user.name());
        System.out.println("   Email: " + user.email());
        System.out.println("   City: " + user.address().city());
        System.out.println("   Company: " + user.company().name());

        // Test 6: Get posts by user
        System.out.println("\n6. Fetching posts by user #1:");
        List<Post> userPosts = jsonPlaceholderClient.getPostsByUserId(1L);
        userPosts.forEach(p -> System.out.println("   - " + p.title()));

        // Test 7: Create a new post
        System.out.println("\n7. Creating a new post:");
        Post newPost =
                new Post(
                        null,
                        1L,
                        "Test Post from Spring HTTP Service Client",
                        "This is a test post created using the declarative HTTP interface");
        Post createdPost = jsonPlaceholderClient.createPost(newPost);
        System.out.println("   Created post with ID: " + createdPost.id());
        System.out.println("   Title: " + createdPost.title());

        // Test 8: Update a post
        System.out.println("\n8. Updating post #1:");
        Post updatedPost = new Post(1L, 1L, "Updated Title", "Updated body content");
        Post result = jsonPlaceholderClient.updatePost(1L, updatedPost);
        System.out.println("   Updated title: " + result.title());

        // Test 9: Delete a post
        System.out.println("\n9. Deleting post #1:");
        jsonPlaceholderClient.deletePost(1L);
        System.out.println("   Post deleted successfully");

        // Test 10: Test error handling (request non-existent resource)
        System.out.println("\n10. Testing error handling (requesting non-existent post):");
        try {
            jsonPlaceholderClient.getPostById(99999L);
        } catch (Exception e) {
            System.out.println("   Error caught: " + e.getClass().getSimpleName());
            System.out.println("   Message: " + e.getMessage());
        }

        System.out.println("\n=== All JSONPlaceholder tests completed! ===\n");
    }
}
