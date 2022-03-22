package com.example.demo.src.user;

import com.example.demo.config.BaseException;
import com.example.demo.src.user.model.Req.PostSignInReq;
import com.example.demo.src.user.model.Req.PostUserReq;
import com.example.demo.src.user.model.Req.PutAddressReq;
import com.example.demo.src.user.model.Res.PostSignInRes;
import com.example.demo.src.user.model.User;
import com.example.demo.src.user.model.UserLocationRes;
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
     * @return BaseResponse<String>
     */
    public void createUser(PostUserReq postUserReq) throws BaseException {

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
            userDao.createUser(postUserReq);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }


    }

    /**
     * 로그인 API
     * [POST] /users/sign-in
     * @return BaseResponse<PostSignInRes>
     */
    public PostSignInRes signIn(PostSignInReq postSignInReq) throws BaseException {
        String encryptPwd;
        try {
            // 사용자에게 바디값으로 받은 비밀번호 암호화
            encryptPwd = new SHA256().encrypt(postSignInReq.getPassward());
            postSignInReq.setPassward(encryptPwd);
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_DECRYPTION_ERROR);
        }

        // 사용자가 존재하는지 확인
        // 존재하지 않을 때
        if(userProvider.checkPassward(postSignInReq.getEmail(),encryptPwd) != 1){
            throw new BaseException(FAILED_TO_LOGIN);
        }

        // DB로부터 정보를 가져옴
        User user = userDao.getUserInfo(postSignInReq);


        try {
            int userIdx = user.getUserIdx();
            String jwt = jwtService.createJwt(userIdx);
            return new PostSignInRes(userIdx,jwt);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 집 주소지 관리 API
     * [PUT] /users/home-address
     * @return BaseResponse<UserLocationRes>
     */
    public UserLocationRes putHomeAddress(int userIdx, PutAddressReq putAddressReq) throws BaseException {
        try {
            UserLocationRes userLocationRes = userDao.putHomeAddress(userIdx, putAddressReq);
            return  userLocationRes;
        } catch (Exception exception) {
            System.out.println("putHomeAddress"+exception);
            throw new BaseException(DATABASE_ERROR);
        }

    }

    /**
     * 회사 주소지 관리 API
     * [PUT] /users/company-address
     * @return BaseResponse<UserLocationRes>
     */
    public UserLocationRes putCompanyAddress(int userIdx, PutAddressReq putAddressReq) throws BaseException {
        try {
            UserLocationRes userLocationRes = userDao.putCompanyAddress(userIdx, putAddressReq);
            return userLocationRes;
        } catch (Exception exception) {
            System.out.println("putCompanyAddress"+exception);
            throw new BaseException(DATABASE_ERROR);
        }

    }
}