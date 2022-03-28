package com.example.demo.src.store.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class PostReviewReq {
    @NotNull(message = "not null userOrderIdx")
    private int userOrderIdx;

    @NotNull(message = "not null score")
    private int score;

    private String content;

    private Map<Integer, String> isMenuGood;
    private Map<Integer, String> reasonForMenu;
    // 싫어요 일 때 선택사항 필요한가?
    private String isDeliveryManGood;
    private String reasonForDelivery;
}
