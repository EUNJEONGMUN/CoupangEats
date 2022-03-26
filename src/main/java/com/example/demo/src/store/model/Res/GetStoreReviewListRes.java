package com.example.demo.src.store.model.Res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetStoreReviewListRes {
    private String userName;
    private int score;
    private String uploadDate;
    private List<String> reviewImg;
    private String content;
    private String orderMenuListString;


}

