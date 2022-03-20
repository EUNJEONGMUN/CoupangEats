package com.example.demo.src.user;

import com.example.demo.config.BaseException;
import com.example.demo.src.user.model.Req.PostUserReq;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.utils.SHA256;
import static com.example.demo.config.BaseResponseStatus.*;

@Service
public class UserService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserDao userDao;
    private final UserProvider userProvider;
    private final JwtService jwtService;
    private final int FAIL = 0;

    @Autowired
    public UserService(UserDao userDao, UserProvider userProvider, JwtService jwtService) {
        this.userDao = userDao;
        this.userProvider = userProvider;
        this.jwtService = jwtService;
    }


    /**
     * 회원 가입 API
     * [POST] /users/sign-up
     * /sign-up?userX=&userY
     * @return BaseResponse<String>
     */
    public void createUser(double userX, double userY, PostUserReq postUserReq) throws BaseException {

        // email 중복 확인
        if(userProvider.checkEmail(postUserReq.getEmail()) == 1){
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }

        // 휴대폰 번호 중복 확인
        if(userProvider.checkPhoneNum(postUserReq.getPhoneNumber()) == 1) {
            throw new BaseException(POST_USERS_EXISTS_PHONE);
        }

        String pwd;

        try {
            pwd = new SHA256().encrypt(postUserReq.getPassward());
            postUserReq.setPassward(pwd);
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }

        try {
            userDao.createUser(userX, userY, postUserReq);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }


    }

}