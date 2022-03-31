package com.example.demo.src.kakao;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.kakao.model.KakaoUserInfo;
import com.example.demo.src.user.UserDao;
import com.example.demo.src.user.UserProvider;
import com.example.demo.src.user.model.Res.PostSignInRes;
import com.example.demo.src.user.model.User;
import com.example.demo.src.user.model.UserNowAddressInfo;
import com.example.demo.utils.JwtService;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.demo.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class KakaoService {

    private final KakaoOAuth kakaoOAuth;

    @Autowired
    public KakaoService(KakaoOAuth kakaoOAuth){
        this.kakaoOAuth = kakaoOAuth;
    }

    public KakaoUserInfo signIn(String code) throws BaseException {
        String accessToken = kakaoOAuth.getAccessToken(code);
        KakaoUserInfo kakaoUserInfo = kakaoOAuth.getUserInfoByToken(accessToken);

        return kakaoUserInfo;
    }


}
