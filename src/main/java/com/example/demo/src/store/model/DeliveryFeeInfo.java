package com.example.demo.src.store.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryFeeInfo {
    private int storeIdx;
    private int minPrice;
    private int maxPrice;
    private int deliveryFee;
}
