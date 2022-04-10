package com.example.demo.src.store;

import com.example.demo.src.store.model.Res.GetStoreHomeRes;
import com.example.demo.src.user.model.UserLocation;

public interface StoreDaoInterface {

    // 홈화면 조회 API
    GetStoreHomeRes getStoreHome(int idx, UserLocation userLocation);

}