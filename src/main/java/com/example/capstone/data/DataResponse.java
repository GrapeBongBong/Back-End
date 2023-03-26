package com.example.capstone.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataResponse {
    private Integer code;
    private HttpStatus httpStatus;
    private String message;
    private Object data;
}
