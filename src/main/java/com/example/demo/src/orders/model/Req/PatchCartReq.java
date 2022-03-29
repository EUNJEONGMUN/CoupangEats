package com.example.demo.src.orders.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PatchCartReq {
    @NotNull(message = "카트 idx를 입력하지 않았습니다.")
    private int cartIdx;
}
