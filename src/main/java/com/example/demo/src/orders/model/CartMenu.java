package com.example.demo.src.orders.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartMenu {
    private int cartIdx;
    private String menuName;
    private String menuOptions;
    private int mulPrice;
    private int orderCount;
}

