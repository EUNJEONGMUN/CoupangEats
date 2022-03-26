package com.example.demo.src.store.model;

import com.example.demo.src.store.model.Res.GetStoreMenuOptionsRes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MenuDetail {
    private int menuIdx;
    private String menuName;
    private int menuPrice;
    private String menuDetail;
    private String menuImgUrl;
    private String isOption;
    private String status;
    private String isManyOrder;
    private String isManyReview;
}
