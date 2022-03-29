package com.example.demo.src.store.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PatchReviewReq {
    @NotEmpty(message = "사용자 주문 idx를 입력하지 않았습니다.")
    private int userOrderIdx;
}
