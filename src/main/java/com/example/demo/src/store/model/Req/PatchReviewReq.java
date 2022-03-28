package com.example.demo.src.store.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class PatchReviewReq {
    @NotNull(message = "not null reviewIdx")
    private int reviewIdx;
    @NotNull(message = "not null score")
    @Size(min=1, max=5, message="별점은 1점에서 5점사이입니다.")
    private int score;

    private String reasonForStore;
    private String content;

    private Map<Integer, String> isMenuGood;
    private Map<Integer, String> reasonForMenu;
    private String isDeliveryManGood;
    private String reasonForDelivery;
    private String status;
}
