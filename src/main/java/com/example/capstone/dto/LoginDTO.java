package com.example.capstone.dto;

import com.sun.istack.NotNull;
import lombok.*;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {

    @NotNull
    private String id;

    @NotNull
    private String password;
}
