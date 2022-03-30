package com.example.demo.src.store.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostLikedReviewReq {
    @NotNull(message="리뷰 idx를 입력해주세요")
    private int reviewIdx;

    @NotBlank(message="상태값을 입력해주세요")
    private String isHelped;
}
