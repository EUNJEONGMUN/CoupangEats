package com.example.demo.src.user;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.user.model.Req.*;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public BaseResponse<String> createUser(@RequestParam double userX, @RequestParam double userY, @RequestBody PostUserReq postUserReq){
        try{
            userService.createUser(userX, userY, postUserReq);
            String result ="";
            return new BaseResponse<>(result);
         }catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }


    }


}
