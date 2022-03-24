package com.example.demo.src.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressInfo {
    private int userAddressIdx;
    private String buildingName="";
    private String address="";
    private String addressDetail="";
    private String addressGuide="";
    private double addressLongitude;
    private double addressLatitude;
    private String addressTitle="";
    private String addressType="";
}
