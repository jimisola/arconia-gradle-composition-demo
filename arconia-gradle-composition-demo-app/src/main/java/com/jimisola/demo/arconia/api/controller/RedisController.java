package com.jimisola.demo.arconia.api.controller;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/redis")
public class RedisController {

    private final RedisTemplate<String, String> stringRedisTemplate;
    private final LongCounter redisOperationsCounter;
    private final LongCounter redisKeysCreatedCounter;
    private final LongCounter redisKeysDeletedCounter;

    public RedisController(RedisTemplate<String, String> stringRedisTemplate, Meter meter) {
        this.stringRedisTemplate = stringRedisTemplate;
        
        // OpenTelemetry metrics - demonstrating direct OTel API usage alongside Micrometer
        this.redisOperationsCounter = meter
            .counterBuilder("redis.operations.total")
            .setDescription("Total number of Redis operations")
            .setUnit("operations")
            .build();
            
        this.redisKeysCreatedCounter = meter
            .counterBuilder("redis.keys.created")
            .setDescription("Number of keys created in Redis")
            .setUnit("keys")
            .build();
            
        this.redisKeysDeletedCounter = meter
            .counterBuilder("redis.keys.deleted")
            .setDescription("Number of keys deleted from Redis")
            .setUnit("keys")
            .build();
    }

    /**
     * Create a key-value pair
     * POST /api/redis/{key}
     */
    @PostMapping("/{key}")
    public ResponseEntity<Map<String, String>> create(@PathVariable String key, @RequestBody ValueRequest request) {
        // Check if key already exists
        boolean exists = Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        
        if (exists) {
            redisOperationsCounter.add(1, io.opentelemetry.api.common.Attributes.builder()
                .put("operation", "create")
                .put("status", "conflict")
                .build());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Key already exists");
            errorResponse.put("key", key);
            errorResponse.put("message", "Use PUT to update existing key");
            
            return ResponseEntity.status(409).body(errorResponse);
        }
        
        stringRedisTemplate.opsForValue().set(key, request.value());
        
        // Increment OpenTelemetry metrics
        redisOperationsCounter.add(1, io.opentelemetry.api.common.Attributes.builder()
            .put("operation", "create")
            .put("status", "success")
            .build());
        redisKeysCreatedCounter.add(1);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Key-value pair stored successfully");
        response.put("key", key);
        
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Get value by key
     * GET /api/redis/{key}
     */
    @GetMapping("/{key}")
    public ResponseEntity<Map<String, String>> get(@PathVariable String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        
        redisOperationsCounter.add(1, io.opentelemetry.api.common.Attributes.builder()
            .put("operation", "get")
            .put("status", value != null ? "success" : "not_found")
            .build());
        
        if (value == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Key not found");
            errorResponse.put("key", key);
            errorResponse.put("message", "The specified key does not exist in Redis");
            
            return ResponseEntity.status(404).body(errorResponse);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("key", key);
        response.put("value", value);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all keys with their values
     * GET /api/redis
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll() {
        Set<String> keys = stringRedisTemplate.keys("*");
        
        Map<String, String> data = new HashMap<>();
        if (keys != null) {
            for (String key : keys) {
                String value = stringRedisTemplate.opsForValue().get(key);
                data.put(key, value);
            }
        }
        
        redisOperationsCounter.add(1, io.opentelemetry.api.common.Attributes.builder()
            .put("operation", "get_all")
            .put("status", "success")
            .build());
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", data.size());
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update a key-value pair
     * PUT /api/redis/{key}
     */
    @PutMapping("/{key}")
    public ResponseEntity<Map<String, String>> update(@PathVariable String key, @RequestBody ValueRequest request) {
        // Check if key exists
        boolean exists = Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        
        redisOperationsCounter.add(1, io.opentelemetry.api.common.Attributes.builder()
            .put("operation", "update")
            .put("status", exists ? "success" : "not_found")
            .build());
        
        if (!exists) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Key not found");
            errorResponse.put("key", key);
            errorResponse.put("message", "Cannot update non-existent key. Use POST to create a new key");
            
            return ResponseEntity.status(404).body(errorResponse);
        }
        
        stringRedisTemplate.opsForValue().set(key, request.value());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Key-value pair updated successfully");
        response.put("key", key);
        response.put("value", request.value());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a key-value pair
     * DELETE /api/redis/{key}
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String key) {
        Boolean deleted = stringRedisTemplate.delete(key);
        
        boolean wasDeleted = Boolean.TRUE.equals(deleted);
        redisOperationsCounter.add(1, io.opentelemetry.api.common.Attributes.builder()
            .put("operation", "delete")
            .put("status", wasDeleted ? "success" : "not_found")
            .build());
        
        if (wasDeleted) {
            redisKeysDeletedCounter.add(1);
        }
        
        if (!wasDeleted) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Key not found");
            errorResponse.put("key", key);
            errorResponse.put("message", "Cannot delete non-existent key");
            
            return ResponseEntity.status(404).body(errorResponse);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Key deleted successfully");
        response.put("key", key);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete all keys
     * DELETE /api/redis
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteAll() {
        Set<String> keys = stringRedisTemplate.keys("*");
        int deletedCount = 0;
        if (keys != null && !keys.isEmpty()) {
            deletedCount = keys.size();
            stringRedisTemplate.delete(keys);
        }
        
        redisOperationsCounter.add(1, io.opentelemetry.api.common.Attributes.builder()
            .put("operation", "delete_all")
            .put("status", "success")
            .build());
        redisKeysDeletedCounter.add(deletedCount);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "All keys deleted successfully");
        response.put("count", String.valueOf(deletedCount));
        
        return ResponseEntity.ok(response);
    }
}
