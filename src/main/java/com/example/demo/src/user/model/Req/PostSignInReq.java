package com.example.demo.src.user.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostSignInReq {
    @NotBlank(message="아이디를 입력해주세요.")
    private String email;

    @NotBlank(message="비밀번호를 입력해주세요.")
    private String passward;
}
