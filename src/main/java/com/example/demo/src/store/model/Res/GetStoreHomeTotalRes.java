package com.example.demo.src.store.model.Res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetStoreHomeTotalRes {
    private List<GetStoreHomeRes> getOnlyEatsStore;
    private List<GetStoreHomeRes> getFranchiseStore;
    private List<GetStoreHomeRes> getNewStore;
    private List<GetStoreHomeRes> getStoreHomeRes;
}
