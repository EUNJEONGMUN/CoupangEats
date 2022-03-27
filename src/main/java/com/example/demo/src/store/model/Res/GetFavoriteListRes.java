package com.example.demo.src.store.model.Res;

import com.example.demo.src.store.model.StoreBestCoupon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetFavoriteListRes {
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

    private int myOrderCount;
    private String myLatelyOrderTime;
    private String addFavoriteStoreTime;
}
