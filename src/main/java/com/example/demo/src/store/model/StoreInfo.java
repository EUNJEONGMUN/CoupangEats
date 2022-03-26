package com.example.demo.src.store.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreInfo {
    private int storeIdx;
    private String storeImgUrl;
    private String storeName;
    private String isCheetah;
    private String timeDelivery;
    private String isToGo;
    private String isCoupon;
    private int minimumPrice;
    private String buildingName;
    private String storeAddress;
    private String storeAddressDetail;
    //    private double storeLongitude;
//    private double storeLatitude;
    private String status;
    private String createdAt;
    private double reviewScore;
    private int reviewCount;
    private double distance;
    private int orderCount;
    private String deliveryFee;


}
