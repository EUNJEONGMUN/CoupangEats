package com.example.demo.src.orders.kakao;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.orders.kakao.model.KakaoOAuth;
import com.example.demo.src.orders.kakao.model.KakaoUserInfo;
import com.example.demo.src.user.model.Res.PostSignInRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:9009")
@RequestMapping("/kakao")
public class KakaoController {
    KakaoOAuth kakaoOAuth = new KakaoOAuth();

    @Autowired
    private final KakaoService kakaoService;


    public KakaoController(KakaoService kakaoService) {
        this.kakaoService = kakaoService;
    }


    @PostMapping(value="/sign-in")
    public BaseResponse<PostSignInRes> kakaoSignIn(@RequestParam("code") String code) throws BaseException {
        KakaoUserInfo userInfo = kakaoOAuth.getUserInfo(code);
        PostSignInRes postSignInRes = kakaoService.signIn(userInfo);
        return new BaseResponse<>(postSignInRes);
    }
}
