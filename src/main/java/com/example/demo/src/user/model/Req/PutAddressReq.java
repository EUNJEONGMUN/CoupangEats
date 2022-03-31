package com.example.demo.src.user.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PutAddressReq {
    @NotEmpty(message = "not empty buildingName")
    private String buildingName;

    @NotEmpty(message = "not empty address")
    private String address;

    @NotEmpty(message = "not empty addressDetail")
    private String addressDetail;

    private String addressGuide;

    @NotNull(message = "not null addressLongitude")
    private double addressLongitude;

    @NotNull(message = "not null addressLatitude")
    private double addressLatitude;

    private String addressTitle;

    private String addressType;

}
