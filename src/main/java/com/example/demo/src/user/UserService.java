package com.example.demo.src.user;

import com.example.demo.config.BaseException;
import com.example.demo.src.user.model.Req.PostAddressReq;
import com.example.demo.src.user.model.Req.*;
import com.example.demo.src.user.model.Res.PostSignInRes;
import com.example.demo.src.user.model.User;
import com.example.demo.src.user.model.UserNowAddressIdx;
import com.example.demo.src.user.model.UserNowAddressInfo;
import com.example.demo.utils.JwtService;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.utils.SHA256;

import java.util.Date;
import java.util.HashMap;

import static com.example.demo.config.BaseResponseStatus.*;
import static com.example.demo.config.secret.MessageKey.*;

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
            pwd = new SHA256().encrypt(postUserReq.getPassword());
            postUserReq.setPassword(pwd);
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }

        try {
            userDao.createUser(postUserReq);
        } catch (Exception exception) {
            System.out.println("createUser --> "+exception);
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
            encryptPwd = new SHA256().encrypt(postSignInReq.getPassword());
            postSignInReq.setPassword(encryptPwd);

        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_DECRYPTION_ERROR);
        }

        // 사용자가 존재하는지 확인
        // 존재하지 않을 때
        if(userProvider.checkPassword(postSignInReq.getEmail(),encryptPwd) != 1){
            throw new BaseException(FAILED_TO_LOGIN);
        }

        // DB로부터 정보를 가져옴
        User user = userDao.getUserInfo(postSignInReq);


        try {
            int userIdx = user.getUserIdx();
            String jwt = jwtService.createJwt(userIdx);

            String refreshToken = jwtService.createRefreshToken(userIdx);
            userDao.saveUserRefreshToken(refreshToken, userIdx); // 저장

            UserNowAddressInfo userNowAddressInfo = userProvider.getUserNowInfo(userIdx);

            return new PostSignInRes(userIdx,jwt,refreshToken,userNowAddressInfo);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
    /**
     * 회원 탈퇴 API
     * [POST] /users/deletion
     *
     * @return BaseResponse<String>
     */
    public void deleteUser(int userIdx, PatchUserReq patchUserReq) throws BaseException {

        String encryptPwd;
        try {
            // 사용자에게 바디값으로 받은 비밀번호 암호화
            encryptPwd = new SHA256().encrypt(patchUserReq.getPassword());
            patchUserReq.setPassword(encryptPwd);
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_DECRYPTION_ERROR);
        }

        if(userProvider.checkPassword(patchUserReq.getEmail(),encryptPwd) != 1){
            throw new BaseException(FAILED_TO_LOGIN);
        }

        try {
            int result = userDao.deleteUser(userIdx);
            if (result == FAIL) {
                throw new BaseException(FAIL_DELETE_USER);
            }
        } catch (Exception exception) {
                throw new BaseException(DATABASE_ERROR);
            }

    }


    /**
     * 회원 정보 확인 API
     * [POST] /users/check-information
     * @return BaseResponse<String>
     */
    public int checkPassword(String email, String password) throws BaseException  {
        String encryptPwd;
        try {
            // 사용자에게 바디값으로 받은 비밀번호 암호화
            encryptPwd = new SHA256().encrypt(password);
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_DECRYPTION_ERROR);
        }

        return userProvider.checkPassword(email, encryptPwd);

    }


    /**
     * 주소지 추가 API
     * [POST] /users/address
     * @return BaseResponse<String>
     */
    public UserNowAddressIdx createAddress(int userIdx, PostAddressReq postAddressReq) throws BaseException {
        try {
            int lastIdx = userDao.createAddress(userIdx, postAddressReq);
            if (lastIdx == FAIL){
                throw new BaseException(FAIL_DELETE_EXISTS_ADDRESS);
            }
            UserNowAddressIdx userNowAddressIdx = userDao.putAddressChoice(userIdx, lastIdx);
            return userNowAddressIdx; // 리턴값 바꾸기
        } catch (Exception exception) {
            System.out.println("createAddress"+exception);
            throw new BaseException(DATABASE_ERROR);
        }

    }

    // 기존 주소 삭제
    public void deleteExistsAddress(int duplicatedAddressIdx) throws BaseException {
        try {
            int result = userDao.deleteExistsAddress(duplicatedAddressIdx);
            if (result == FAIL){
                throw new BaseException(FAIL_DELETE_EXISTS_ADDRESS);
            }
        } catch (Exception exception) {
            System.out.println("deleteExistsAddress"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 주소지 수정 API
     * [PUT] /users/address
     * @return BaseResponse<String>
     */
    public void modifyAddress(int userIdx, int addressIdx, PutAddressReq putAddressReq) throws BaseException {
        try {
            int result = userDao.modifyAddress(userIdx, addressIdx, putAddressReq);
            if (result == FAIL){
                throw new BaseException(FAIL_MODIFY_ADDRESS);
            }
        } catch (Exception exception) {
            System.out.println("deleteExistsAddress"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 주소지 삭제 API
     * [PATCH] /users/address/deletion
     * @return BaseResponse<String>
     */
    public void deleteAddress(int addressIdx) throws BaseException  {
        try {
            int result = userDao.deleteAddress(addressIdx);
            if (result == FAIL){
                throw new BaseException(FAIL_DELETE_ADDRESS);
            }
        } catch (Exception exception) {
            System.out.println("deleteAddress"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }



    /**
     * 현재 주소지 변경 API
     * [PUT] /users/address/choice
     * /choice?addressIdx=
     * @return BaseResponse<String>
     */
    public UserNowAddressIdx putAddressChoice(int userIdx, int addressIdx) throws BaseException {
        try {
            UserNowAddressIdx userNowAddressIdx = userDao.putAddressChoice(userIdx, addressIdx);
            return userNowAddressIdx;
        } catch (Exception exception) {
            System.out.println("putAddressChoice"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 할인 쿠폰 받기 API
     * [POST] /users/coupons
     * /coupons?couponIdx=?
     * @return BaseResponse<String>
     */
    public void createUserCoupon(int userIdx, int couponIdx) throws BaseException {
        try {
              int result = userDao.createUserCoupon(userIdx, couponIdx);
              if (result==FAIL){
                  throw new BaseException(FAIL_CREATE_USER_COUPON);
              }
        } catch (Exception exception) {
            System.out.println("createUserCoupon"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 휴대폰 인증 API
     * [POST] /users/message
     * @return BaseResponse<String>
     */
    public void certifiedPhoneNumber(String phoneNumber, String numStr) throws BaseException  {
        String api_key = API_KEY;
        String api_secret = API_SECRET;
        net.nurigo.java_sdk.api.Message coolsms = new Message(api_key, api_secret);

        // 4 params(to, from, type, text) are mandatory. must be filled
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("to", phoneNumber);    // 수신전화번호
        params.put("from", PHONE_NUMBER);    // 발신전화번호.
        params.put("type", "SMS");
        params.put("text", "회원가입 인증 메시지 : 인증번호는" + "["+numStr+"]" + "입니다.");

        try {
            JSONObject obj = (JSONObject) coolsms.send(params);
            System.out.println(obj.toString());
        } catch (CoolsmsException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getCode());
            throw new BaseException(FAIL_SEND_MESSAGE);
        }
    }

    // 휴대폰 인증 번호 저장
    public void certifiedPhoneNumberSave(String phoneNumber, String numStr) throws BaseException {
        try {
            int result = userDao.certifiedPhoneNumberSave(phoneNumber, numStr);
        } catch (Exception exception) {
            System.out.println("certifiedPhoneNumberSave"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // access token 재발급
    public PostSignInRes refreshToken(String token,String refreshToken) throws BaseException {

        System.out.println(refreshToken);
//        String token = jwtService.getJwt();
        if(!jwtService.validateTokenExceptExpiration(token)) throw new BaseException(NOT_EXPIRATION_TOKEN);

        System.out.println("여기");

        int userIdx = jwtService.getUserIdxToken(refreshToken);
        System.out.println("userIdx>>>"+userIdx);

        String dbRefreshToken = userDao.findRefreshToken(userIdx);
        String newJwtToken = jwtService.createJwt(userIdx);

        UserNowAddressInfo userNowAddressInfo = userProvider.getUserNowInfo(userIdx);


        if(!refreshToken.equals(dbRefreshToken)){
            throw new BaseException(INVALID_REFRESH_TOKEN);
        }
        if (!jwtService.validateTokenExceptExpiration(dbRefreshToken)){
            return new PostSignInRes(userIdx,newJwtToken,dbRefreshToken,userNowAddressInfo);
        }
        String newRefreshToken =  jwtService.createRefreshToken(userIdx);
        userDao.saveUserRefreshToken(newRefreshToken, userIdx);

        return new PostSignInRes(userIdx,newJwtToken,newRefreshToken,userNowAddressInfo);
    }

    /**
     * 로그아웃 API
     * [POST] /users/sign-out
     * @return BaseResponse<PostSignInRes>
     * @param userIdx
     */
    public void signOut(int userIdx) throws BaseException {
        try {
            userDao.deleteRefreshToken(userIdx);
            Date expiration = jwtService.getExpiration();
            String accessToken = jwtService.getJwt();
            userDao.insertBlackList(accessToken,expiration);
        } catch (Exception exception) {
            System.out.println("signOut"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }
}