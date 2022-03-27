package com.example.demo.src.store.model.Res;

import com.example.demo.src.store.model.BossReview;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetStoreReviewListRes {
    private String reviewUserName;
    private int reviewIdx;
    private int score;
    private String uploadDate;
    private String createdAt;
    private String content;
    private String orderMenuListString;
    private List<String> reviewImg;
    private BossReview bossReview;
    private int helpedCount;
    private String isMyHelped; // 내가 좋아요 눌렀는지
    private String isMyReview; // 내가 쓴 리뷰인지
    private String isPhotoReview;




}

