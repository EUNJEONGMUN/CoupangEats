package com.example.demo.src.store.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.sql.Time;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreCouponInfo {
    private int couponIdx;
    private int storeIdx;
    private String couponTitle;
    private int discountPrice;
    private int limitPrice;
    private Time endDate;
    private String couponType;
    private Time createdAt;
    private String status;
}
