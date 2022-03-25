package com.example.demo.src.orders.model.Req;

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
public class PutModifyCartReq {
    @NotNull(message="수량을 입력해주세요.")
    private int changeCount;
    private String status;
}
