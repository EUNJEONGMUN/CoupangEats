package com.example.demo.src.store.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MenuDetail {
    private String menuName;
    private int menuPrice;
    private String menuDetail;
    private String menuImgUrl;
}
