package com.example.demo.src.store.model.Res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetStoreHomeRes {
    private String storeImgUrl;
    private String storeName;
    private String isCheetah;
    private String timeDelivery;
    private double reviewScore;
    private int reviewCount;
    private int fee;
    private String isToGo;
    private String isCoupon;
    private List<String> menuImgUrl;
}
