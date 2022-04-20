package com.example.demo.src.store.model.Res;

import com.example.demo.src.store.model.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetStoreHomeRes {
    private StoreInfo storeInfo;
    private StoreBestCoupon storeBestCoupon;
}
