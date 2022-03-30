package com.example.demo.src.user.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostMessageCheckReq {
    private String phoneNumber;
    @NotNull(message="인증번호를 입력해주세요.")
    private int certificationNum;
}
