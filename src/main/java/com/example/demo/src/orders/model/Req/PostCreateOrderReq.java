package com.example.demo.src.orders.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostCreateOrderReq {
    private int storeIdx;
    private int couponIdx;
    private String message;
    private String isSpoon;
    @NotEmpty(message = "notEmpty deliveryManOptionIdx")
    private int deliveryManOptionIdx;

    private String deliveryManContent;


}
