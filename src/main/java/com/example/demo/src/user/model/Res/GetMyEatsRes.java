package com.example.demo.src.user.model.Res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetMyEatsRes {
    private String userName;
    private String phoneNumber;
    private int couponCount;
}
