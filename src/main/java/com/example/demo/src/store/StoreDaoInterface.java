package com.example.demo.src.store;

import com.example.demo.src.store.model.Req.GetStoreHomeReq;
import com.example.demo.src.store.model.Res.GetStoreHomeRes;
import com.example.demo.src.user.model.UserLocation;

import java.util.List;

public interface StoreDaoInterface {

    // 홈화면 조회 API
    GetStoreHomeRes getStoreHome(int idx, UserLocation userLocation);

    // 사용자의 현재 위치 찾기
    UserLocation getNowUserLocation(int userIdx);

    // 홈 화면 조회 -> 기본 홈 가게 리스트
    List<Integer> findStoreIdxList(int categoryIdx, UserLocation userLocation, GetStoreHomeReq getStoreHomeReq);

    // 홈화면 조회 -> 이츠에만 있는 맛집 가게 리스트
    List<Integer> findOnlyEatsStoreIdxList();

    // 홈화면 조회 -> 인기있는 프랜차이즈 가게 리스트
    List<Integer> findFranchiseStoreIdxList();

    // 홈화면 조회 -> 새로 들어왔어요 가게 리스트
    List<Integer> findNewStoreIdxList();









}
