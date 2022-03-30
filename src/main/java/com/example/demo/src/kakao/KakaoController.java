package com.example.demo.src.kakao;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.kakao.model.KakaoOAuth;
import com.example.demo.src.kakao.model.KakaoUserInfo;
import com.example.demo.src.user.model.Res.PostSignInRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

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


//    @RequestMapping(value="/sign-in/back")  //localhost:
//    public ModelAndView login(@RequestParam("code") String code, HttpSession session) {
//        ModelAndView mav = new ModelAndView();
//        // 1. 인증코드 요청 및 전달
//        String accessToken = kakaoOAuth.getAccessToken(code);
//        // 2. 인증코드로 토큰 전달
//        KakaoUserInfo userInfo = kakaoOAuth.getUserInfoByToken(accessToken);
//
//        System.out.println("login info : "+ userInfo.toString());
//
//        if(userInfo.getEmail() != null){
//            session.setAttribute("userId", userInfo.getEmail());
//            session.setAttribute("access_token", accessToken);
//        }
//        mav.addObject("userId", userInfo.getEmail());
//        mav.setViewName("index");
//        return mav;
//    }

    @GetMapping(value="/sign-in")
    public BaseResponse<PostSignInRes> kakaoSignIn(@RequestParam("code") String code) throws BaseException {
        KakaoUserInfo userInfo = kakaoOAuth.getUserInfo(code);
        PostSignInRes postSignInRes = kakaoService.signIn(userInfo);
        return new BaseResponse<>(postSignInRes);
    }
}
