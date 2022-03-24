package com.example.demo.src.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserNowAddressInfo {
    private int userAddressIdx;
    private String buildingName ="";
    private String address ="";
    private String addressDetail ="";
    private String addressGuide ="";
    private String addressTitle ="";
    private double addressLongitude;
    private double addressLatitude;
    private String addressType="";
}
