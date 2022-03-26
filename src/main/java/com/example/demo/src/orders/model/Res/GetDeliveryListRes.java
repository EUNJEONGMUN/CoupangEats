package com.example.demo.src.orders.model.Res;

import com.example.demo.src.orders.model.OrderMenuInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetDeliveryListRes {
    private int storeIdx;
    private String storeImgUrl;
    private String storeName;
    private String orderTime;
    private int totalPrice;
    private String status;
    private int ReviewScore;
    private List<OrderMenuInfo> orderMenuInfo;
}
