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
public class PostCreateCartReq {
    private String menuOptions;
    private int orderCount;
    @NotNull(message="not null orderPrice")
    private int orderPrice; // 옵션 가격까지 합한 가격

}
