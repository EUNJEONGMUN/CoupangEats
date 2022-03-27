package com.example.demo.src.user.model.Res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetUserCouponListRes {
    private int userCouponIdx;
    private int storeIdx;
    private String couponTitle;
    private int discountPrice;
    private int limitPrice;
    private String endDate;
    private String couponType;

}
