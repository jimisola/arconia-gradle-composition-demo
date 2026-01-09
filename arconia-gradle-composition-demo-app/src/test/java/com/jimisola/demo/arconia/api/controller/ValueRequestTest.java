package com.jimisola.demo.arconia.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class ValueRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateValueRequest() {
        var request = new ValueRequest("testValue");
        
        assertThat(request.value()).isEqualTo("testValue");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        var request = new ValueRequest("testValue");
        
        String json = objectMapper.writeValueAsString(request);
        
        assertThat(json).isEqualTo("{\"value\":\"testValue\"}");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        String json = "{\"value\":\"testValue\"}";
        
        ValueRequest request = objectMapper.readValue(json, ValueRequest.class);
        
        assertThat(request.value()).isEqualTo("testValue");
    }

    @Test
    void shouldHandleNullValue() throws Exception {
        String json = "{\"value\":null}";
        
        ValueRequest request = objectMapper.readValue(json, ValueRequest.class);
        
        assertThat(request.value()).isNull();
    }

    @Test
    void shouldSupportRecordEquality() {
        var request1 = new ValueRequest("testValue");
        var request2 = new ValueRequest("testValue");
        var request3 = new ValueRequest("differentValue");
        
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }
}
