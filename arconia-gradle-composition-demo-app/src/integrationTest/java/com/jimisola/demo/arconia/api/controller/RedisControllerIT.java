package com.jimisola.demo.arconia.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class RedisControllerIT {

    @Autowired
    private RestTestClient client;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        // Clean up Redis before each test
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    void shouldCreateKeyValuePair() {
        var request = Map.of("value", "testValue");

        client.post()
            .uri("/api/redis/testKey")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.message").isEqualTo("Key-value pair stored successfully")
            .jsonPath("$.key").isEqualTo("testKey");

        // Verify value was stored
        String storedValue = redisTemplate.opsForValue().get("testKey");
        assertThat(storedValue).isEqualTo("testValue");
    }

    @Test
    void shouldReturnConflictWhenCreatingExistingKey() {
        // Setup - create initial key
        redisTemplate.opsForValue().set("existingKey", "existingValue");

        var request = Map.of("value", "newValue");

        client.post()
            .uri("/api/redis/existingKey")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Key already exists")
            .jsonPath("$.key").isEqualTo("existingKey")
            .jsonPath("$.message").isEqualTo("Use PUT to update existing key");

        // Verify original value was not changed
        String storedValue = redisTemplate.opsForValue().get("existingKey");
        assertThat(storedValue).isEqualTo("existingValue");
    }

    @Test
    void shouldGetValueByKey() {
        // Setup
        redisTemplate.opsForValue().set("myKey", "myValue");

        client.get()
            .uri("/api/redis/myKey")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.key").isEqualTo("myKey")
            .jsonPath("$.value").isEqualTo("myValue");
    }

    @Test
    void shouldReturnNotFoundForNonExistentKey() {
        client.get()
            .uri("/api/redis/nonExistentKey")
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.error").isEqualTo("Key not found")
            .jsonPath("$.key").isEqualTo("nonExistentKey")
            .jsonPath("$.message").isEqualTo("The specified key does not exist in Redis");
    }

    @Test
    void shouldGetAllKeys() {
        // Setup
        redisTemplate.opsForValue().set("key1", "value1");
        redisTemplate.opsForValue().set("key2", "value2");
        redisTemplate.opsForValue().set("key3", "value3");

        client.get()
            .uri("/api/redis")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.count").isEqualTo(3)
            .jsonPath("$.data.key1").isEqualTo("value1")
            .jsonPath("$.data.key2").isEqualTo("value2")
            .jsonPath("$.data.key3").isEqualTo("value3");
    }

    @Test
    void shouldReturnEmptyListWhenNoKeys() {
        client.get()
            .uri("/api/redis")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.count").isEqualTo(0)
            .jsonPath("$.data").isEmpty();
    }

    @Test
    void shouldUpdateExistingKey() {
        // Setup
        redisTemplate.opsForValue().set("updateKey", "oldValue");

        var request = Map.of("value", "newValue");

        client.put()
            .uri("/api/redis/updateKey")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.message").isEqualTo("Key-value pair updated successfully")
            .jsonPath("$.key").isEqualTo("updateKey")
            .jsonPath("$.value").isEqualTo("newValue");

        // Verify value was updated
        String updatedValue = redisTemplate.opsForValue().get("updateKey");
        assertThat(updatedValue).isEqualTo("newValue");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentKey() {
        var request = Map.of("value", "newValue");

        client.put()
            .uri("/api/redis/nonExistentKey")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.error").isEqualTo("Key not found")
            .jsonPath("$.key").isEqualTo("nonExistentKey")
            .jsonPath("$.message").isEqualTo("Cannot update non-existent key. Use POST to create a new key");
    }

    @Test
    void shouldDeleteKey() {
        // Setup
        redisTemplate.opsForValue().set("deleteKey", "deleteValue");

        client.delete()
            .uri("/api/redis/deleteKey")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.message").isEqualTo("Key deleted successfully")
            .jsonPath("$.key").isEqualTo("deleteKey");

        // Verify key was deleted
        Boolean exists = redisTemplate.hasKey("deleteKey");
        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentKey() {
        client.delete()
            .uri("/api/redis/nonExistentKey")
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.error").isEqualTo("Key not found")
            .jsonPath("$.key").isEqualTo("nonExistentKey")
            .jsonPath("$.message").isEqualTo("Cannot delete non-existent key");
    }

    @Test
    void shouldDeleteAllKeys() {
        // Setup
        redisTemplate.opsForValue().set("key1", "value1");
        redisTemplate.opsForValue().set("key2", "value2");
        redisTemplate.opsForValue().set("key3", "value3");

        client.delete()
            .uri("/api/redis")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.message").isEqualTo("All keys deleted successfully")
            .jsonPath("$.count").isEqualTo("3");

        // Verify all keys were deleted
        assertThat(redisTemplate.keys("*")).isEmpty();
    }

    @Test
    void shouldHandleDeleteAllWhenNoKeys() {
        client.delete()
            .uri("/api/redis")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.message").isEqualTo("All keys deleted successfully")
            .jsonPath("$.count").isEqualTo("0");
    }
}
