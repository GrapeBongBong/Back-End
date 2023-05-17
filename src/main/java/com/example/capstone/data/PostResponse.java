package com.example.capstone.data;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class PostResponse {
    private static final ObjectNode responseJson = JsonNodeFactory.instance.objectNode();

    public static ResponseEntity<?> notExistPost(String message) {
        responseJson.put("message", message);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseJson);
    }
}
