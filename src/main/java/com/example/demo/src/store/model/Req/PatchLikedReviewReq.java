package com.example.demo.src.store.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PatchLikedReviewReq {
    @NotNull(message="리뷰 idx를 입력해주세요")
    private int reviewIdx;
}
