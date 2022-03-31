package com.example.demo.src.store.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetFavoriteList {
    private int storeIdx;
    private String storeImgUrl;
    private String storeName;
    private String isCheetah;
    private String timeDelivery;
    private String isToGo;
    private String isCoupon;
    private double distance;
    private String status;
    private double reviewScore;
    private int reviewCount;

    private String deliveryFee;

    private String storeBestCoupon;
    private String isNewStore;
}
