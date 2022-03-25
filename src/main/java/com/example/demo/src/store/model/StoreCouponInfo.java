package com.example.demo.src.store.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.DateTimeException;

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
    private String endDate;
    private String couponType;
    private String createdAt;
    private String status;
}
