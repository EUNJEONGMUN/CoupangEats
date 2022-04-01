package com.example.demo.src.orders.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderMenuInfo {
    private int cartIdx;
    private int orderCount;
    private String menuName;
    private String menuOptions;
    private String isGood;
    private int mulPrice;

}
