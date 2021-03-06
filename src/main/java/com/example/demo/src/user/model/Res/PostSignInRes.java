package com.example.demo.src.user.model.Res;

import com.example.demo.src.user.model.UserNowAddressInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostSignInRes {
    private int userIdx;
    private String jwt;
    private String refreshToken;
    private UserNowAddressInfo userNowAddressInfo;
}
