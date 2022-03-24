package com.example.demo.src.orders.model.Res;

import com.example.demo.src.orders.model.CartMenu;
import com.example.demo.src.orders.model.CartStoreInfo;
import com.example.demo.src.user.model.UserLocationRes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetCartListRes {
//    private UserLocationRes userLocationRes;
    private String storeName;
    private String isCheetah;
    private int minimumPrice;
    private String timeDelivery;
    private String timeToGo;
    private int totalPrice;
    private int deliveryFee;

//    private CartStoreInfo cartStoreInfo;
    private List<CartMenu> cartMenu;

}
