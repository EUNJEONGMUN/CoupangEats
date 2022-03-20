package com.example.demo.src.user.model.Req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostUserReq {

    @NotBlank(message="이메일을 입력하세요.")
    private String email;

    @NotBlank(message="비밀번호를 입력해주세요.")
    @Size(min=8, max=20, message = "8이상 20자 이하로 입력해주세요.")
    private String passward;

    @NotBlank(message="이름을 입력해주세요.")
    private String userName;

    @NotBlank(message="휴대폰 번호를 입력해주세요.")
    @Pattern(regexp = "^010([0-9]{3,4})([0-9]{4})$", message = "휴대폰 번호 형식을 확인해주세요.")
    private String phoneNumber;
}
