package com.example.demo.src.user;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.user.model.Req.*;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.example.demo.config.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.isRegexEmail;
import static com.example.demo.utils.ValidationRegex.isRegexPwd;

@RestController
@RequestMapping("/users")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final  UserProvider userProvider;
    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtService jwtService;

    public UserController(UserProvider userProvider, UserService userService, JwtService jwtService){
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * 회원 가입 API
     * [POST] /users/sign-up
     * /sign-up?userX=&userY
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/sign-up")
    public BaseResponse<String> createUser(@RequestParam double userX, @RequestParam double userY, @Valid @RequestBody PostUserReq postUserReq){
        try{
            // 이메일 정규식 확인
            if(!isRegexEmail(postUserReq.getEmail())){
                return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
            }

            // 비밀번호 정규식 확인
            if(!isRegexPwd(postUserReq.getPassward())){
                return new BaseResponse<>(POST_USERS_INVALID_PWD);
            }

            String userEmail = postUserReq.getEmail().substring(0,postUserReq.getEmail().lastIndexOf("@"));

            // 아이디(이메일)와 비밀번호 동일 여부 체크
            if (postUserReq.getPassward().contains(userEmail)) {
                return new BaseResponse<>(PWD_CONTAINS_EMAIL);
            }

            // 3개 이상 동일 여부 체크
            // 구현 해야 함.

            userService.createUser(userX, userY, postUserReq);
            String result ="";
            return new BaseResponse<>(result);
         }catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }


    }


}
