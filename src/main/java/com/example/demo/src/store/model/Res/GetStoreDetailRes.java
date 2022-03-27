package com.example.demo.src.store.model.Res;

import com.example.demo.src.store.model.DeliveryFeeInfo;
import com.example.demo.src.store.model.MenuCategory;
import com.example.demo.src.store.model.PhotoReview;
import com.example.demo.src.store.model.StoreCouponInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetStoreDetailRes {
    private int storeIdx;
    private List<String> storeImgUrl;
    private String storeName;
    private String isCheetah;
    private String timeDelivery;
    private String isToGo;
    private String isCoupon;
    private int minimumPrice;
    private String buildingName;
    private String storeAddress;
    private String storeAddressDetail;
    private double distance;
    private String status;
//    private String createdAt;
    private double reviewScore;
    private int reviewCount;
    private String timeToGo;
    private List<StoreCouponInfo> storeCouponInfo;
    private List<DeliveryFeeInfo> deliveryFeeInfo;
    private List<PhotoReview> photoReview;
    private List<MenuCategory> menuCategory;
    private String isFavoriteStore;
}
