package com.example.demo.src.orders.model.Res;

import com.example.demo.src.orders.model.CartAddressInfo;
import com.example.demo.src.orders.model.CartMenu;
import com.example.demo.src.orders.model.DeliveryFeeList;
import com.example.demo.src.store.model.DeliveryFeeInfo;
import com.example.demo.src.user.model.AddressInfo;
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
    private CartAddressInfo nowAddress;
    private int storeIdx;
    private String storeName;
    private String isCheetah;
    private int minimumPrice;
    private String timeDelivery;
    private String timeToGo;
    private String status;
    private String buildingName;
    private String storeAddress;
    private String storeAddressDetail;
    private double distance;
    private int totalPrice;
    private List<DeliveryFeeList> deliveryFeeList;
//    private CartStoreInfo cartStoreInfo;
    private List<CartMenu> cartMenu;

}
