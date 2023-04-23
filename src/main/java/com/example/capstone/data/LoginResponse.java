package com.example.capstone.data;

import com.example.capstone.dto.UserDTO;
import com.example.capstone.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private Integer code;
    private HttpStatus httpStatus;
    private String message;
    private String token;
    private UserEntity user;
}
