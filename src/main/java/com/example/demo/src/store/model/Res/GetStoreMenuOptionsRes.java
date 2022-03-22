package com.example.demo.src.store.model.Res;

import com.example.demo.src.store.model.MenuOptions;
import com.example.demo.src.store.model.MenuOptionsDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetStoreMenuOptionsRes {
    private String menuImgUrl;
    private String menuName;
    private String menuDetail;
    private int menuPrice;
    private List<MenuOptions> menuOptions;
//    private List<MenuOptionsDetail> menuOptionsDetail;



}
