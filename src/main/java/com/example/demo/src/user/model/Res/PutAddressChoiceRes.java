package com.example.demo.src.user.model.Res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PutAddressChoiceRes {
    private String buildingName="";
    private String address="";
    private String addressDetail="";
    private String addressGuide="";
    private double addressLongitude;
    private double addressLatitude;
    private String addressTitle="";
    private String addressType="";

}
