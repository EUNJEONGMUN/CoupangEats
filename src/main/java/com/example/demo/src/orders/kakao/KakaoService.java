package com.example.demo.src.orders.kakao;

import com.example.demo.config.BaseException;
import com.example.demo.src.orders.kakao.model.KakaoOAuth;
import com.example.demo.src.orders.kakao.model.KakaoUserInfo;
import com.example.demo.src.user.UserDao;
import com.example.demo.src.user.UserProvider;
import com.example.demo.src.user.model.Res.PostSignInRes;
import com.example.demo.src.user.model.User;
import com.example.demo.src.user.model.UserNowAddressInfo;
import com.example.demo.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.demo.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class KakaoService {

    private final UserProvider userProvider;
    private final UserDao userDao;
    private final JwtService jwtService;

    @Autowired
    public KakaoService(UserProvider userProvider, UserDao userDao, JwtService jwtService){
        this.userProvider = userProvider;
        this.userDao = userDao;
        this.jwtService = jwtService;

    }

    public PostSignInRes signIn(KakaoUserInfo userInfo) throws BaseException{
        User user = userDao.getUserInfoKakao(userInfo);
        try {
            int userIdx = user.getUserIdx();
            String jwt = jwtService.createJwt(userIdx);

            UserNowAddressInfo userNowAddressInfo = userProvider.getUserNowInfo(userIdx);

            return new PostSignInRes(userIdx,jwt, userNowAddressInfo);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }

    }
}
