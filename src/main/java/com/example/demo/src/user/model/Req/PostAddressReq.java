package com.example.demo.src.user.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostAddressReq {
    @NotNull(message = "not null buildingName")
    private String buildingName;

    @NotNull(message = "not null address")
    private String address;

    @NotNull(message = "not null addressDetail")
    private String addressDetail;

    @NotNull(message = "not null addressGuide")
    private String addressGuide;

    @NotNull(message = "not null addressLongitude")
    private double addressLongitude;

    @NotNull(message = "not null addressLatitude")
    private double addressLatitude;

    private String addressTitle;

    private String addressType;

}
