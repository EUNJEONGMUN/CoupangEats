package com.example.demo.src.store.model.Res;

import com.example.demo.src.store.model.GetFavoriteList;
import com.example.demo.src.store.model.StoreBestCoupon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetFavoriteListRes {
    private List<GetFavoriteList> getFavoriteListList;
    private String sortType;

//
//    private int myOrderCount;
//    private String myLatelyOrderTime;
//    private String addFavoriteStoreTime;
}
