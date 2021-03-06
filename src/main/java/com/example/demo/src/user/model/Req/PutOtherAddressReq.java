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
public class PutOtherAddressReq {
    @NotBlank(message = "not blank address")
    private String address;

    @NotNull(message = "not null addressDetail")
    private String addressDetail;

    @NotNull(message = "not null addressGuide")
    private String addressGuide;

    @NotNull(message = "not null userLongitude")
    private double userLongitude;

    @NotNull(message = "not null userLatitude")
    private double userLatitude;

    private String addressTitle;

    private String status;
}
