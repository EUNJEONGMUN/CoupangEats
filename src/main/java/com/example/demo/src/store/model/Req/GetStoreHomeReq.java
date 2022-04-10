package com.example.demo.src.store.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

@Getter
@Setter
@NoArgsConstructor
public class GetStoreHomeReq {
    private int categoryIdx;
    private String sort;
    private String isCheetah;
    private int deliveryFee;
    private int minimumPrice;
    private String isToGo;
    private String isCoupon;

    public GetStoreHomeReq(int categoryIdx, String sort, String isCheetah, String deliveryFee, String minimumPrice, String isToGo, String isCoupon) {
        this.categoryIdx = categoryIdx;
        this.sort = sort;
        this.isCheetah = isCheetah;
        if (deliveryFee.equals("전체")){
            this.deliveryFee = 100000;
        } else if (deliveryFee.equals("무료배달")){
            this.deliveryFee = 0;
        } else {
            this.deliveryFee = Integer.parseInt(deliveryFee);
        }
        if (minimumPrice.equals("전체")){
            this.minimumPrice = 100000;
        } else{
            this.minimumPrice = Integer.parseInt(minimumPrice);
        }
        this.isToGo = isToGo;
        this.isCoupon = isCoupon;
    }
}

