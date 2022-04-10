package com.example.demo.src.orders.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostCreateOrderReq {
    @NotNull(message = "userAddressIdx를 입력해주세요.")
    private int userAddressIdx;

    @NotNull(message = "storeIdx를 입력해주세요.")
    private int storeIdx;

    private int couponIdx;

    private String message;

    private String isSpoon;
    @NotNull(message = "notEmpty deliveryManOptionIdx")
    private int deliveryManOptionIdx;

    private String deliveryManContent;

    private int deliveryFee;


}
