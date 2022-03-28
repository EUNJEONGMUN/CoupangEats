package com.example.demo.src.store.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PostReviewReq {
    @NotNull(message = "not null score")
    private int score;

    private String content;

    private List<String> reviewImgUrl;

    private List<String> isMenuGood;

    // 싫어요 일 때 선택사항 필요한가?
    private String isDeliveryManGood;
    private List<String> whyDeliveryManBad;

}
