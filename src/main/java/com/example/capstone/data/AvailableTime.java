package com.example.capstone.data;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class AvailableTime {
    @NotNull
    private String[] days;

    @NotBlank
    private String timezone;
}
