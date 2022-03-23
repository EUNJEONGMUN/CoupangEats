package com.example.demo.src.user.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PutAddressChoiceReq {
    @NotNull(message = "not blank userLongitude")
    private double userLongitude;

    @NotNull(message = "not blank userLatitude")
    private double userLatitude;

}
