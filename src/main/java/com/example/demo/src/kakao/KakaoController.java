package com.example.demo.src.kakao;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.kakao.model.KakaoUserInfo;
import com.example.demo.src.user.UserProvider;
import com.example.demo.src.user.model.Res.PostSignInRes;
import com.example.demo.src.user.model.User;
import com.example.demo.src.user.model.UserNowAddressInfo;
import com.example.demo.utils.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.config.BaseResponseStatus.DUPLICATED_EMAIL;
import static com.example.demo.config.BaseResponseStatus.EMPTY_USER_EMAIL;

@RestController("/kakao")
@CrossOrigin(origins = "http://localhost:9009")
@RequiredArgsConstructor
public class KakaoController {

    @Autowired
    private final KakaoService kakaoService;
    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final JwtService jwtService;
    @Autowired
    private final KakaoOAuth kakaoOAuth;

    @RequestMapping(value="/sign-in")
    public BaseResponse<PostSignInRes> kakaoSignIn(@RequestParam("code") String code) throws BaseException {
        System.out.println("here");
        System.out.println(">>>>"+code+"<<<");
        KakaoUserInfo userInfo = kakaoService.signIn(code);
        if (userInfo.getEmail()==null || userProvider.checkUserByEmail(userInfo.getEmail()) == 0){
            return new BaseResponse<>(EMPTY_USER_EMAIL);
        }

        User user = userProvider.getUserInfoKakao(userInfo.getEmail());

        int userIdx = user.getUserIdx();
        String jwt = jwtService.createJwt(userIdx);

        UserNowAddressInfo userNowAddressInfo = userProvider.getUserNowInfo(userIdx);

        return new BaseResponse<>(new PostSignInRes(userIdx,jwt, userNowAddressInfo));

    }
}
