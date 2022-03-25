package com.example.demo.src.store.model.Res;

import com.example.demo.src.store.model.MenuCategory;
import com.example.demo.src.store.model.PhotoReview;
import com.example.demo.src.store.model.StoreCouponInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetStoreDetailRes {
    private List<PhotoReview> photoReview;
    private List<MenuCategory> menuCategory;
}
