package com.example.demo.src.store.model.Res;

import com.example.demo.src.store.model.MenuCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetStoreDetailRes {
    private String storeImgUrl;
    private String storeName;
    private String isCheetah;
    private String timeDelivery;
    private double reviewScore;
    private int reviewCount;
    private int fee;
    private int minimumPrice;
    private String maxDiscountCoupon;
    private List<MenuCategory> menuCategory;
}
