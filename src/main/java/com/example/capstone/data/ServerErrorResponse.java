package com.example.capstone.data;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class ServerErrorResponse {
    private static final ObjectNode responseJson = JsonNodeFactory.instance.objectNode();

    public static ResponseEntity<?> handleServerError(String message) {
        responseJson.put("message", message);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseJson);
    }
}
