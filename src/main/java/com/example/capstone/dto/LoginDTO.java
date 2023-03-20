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
    @Size(min = 3, max = 50)
    private String id;

    @NotNull
    @Size(min = 3, max = 100)
    private String password;
}
