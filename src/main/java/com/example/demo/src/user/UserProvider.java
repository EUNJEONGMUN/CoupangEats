package com.example.demo.src.user;

import com.example.demo.config.BaseException;
import com.example.demo.src.user.model.Res.GetUserAddressRes;
import com.example.demo.src.user.model.Res.GetUserCouponListRes;
import com.example.demo.src.user.model.UserNowAddressInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class UserProvider {
    private final UserDao userDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UserProvider(UserDao userDao) {
        this.userDao = userDao;
    }


    /**
     * 주소지 조회 API
     * [GET] /users/address
     * @return BaseResponse<GetUserAddressRes>
     */
    public GetUserAddressRes getUserAddress(int userIdx) throws BaseException {
        try {
            GetUserAddressRes getUserAddressRes = userDao.getUserAddress(userIdx);
            return getUserAddressRes;
        } catch (Exception exception) {
            System.out.println("getUserAddress-> "+ exception);
            throw new BaseException(DATABASE_ERROR);
        }

    }

    /**
     * 할인 쿠폰 조회 API
     * [GET] /users/coupons
     * @return BaseResponse<List<GetUserCouponListRes>>
     */
    public List<GetUserCouponListRes> getUserCoupons(int userIdx) throws BaseException {
        try {
            List<GetUserCouponListRes> GetUserCouponListRes = userDao.getUserCoupons(userIdx);
            return GetUserCouponListRes;
        } catch (Exception exception) {
            System.out.println("getUserCoupons-> "+ exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 이메일 중복 확인
    public int checkEmail(String email) throws BaseException {
        try {
            return userDao.checkEmail(email);
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }

    }

    // 휴대폰 번호 중복 확인
    public int checkPhoneNum(String phoneNumber) throws BaseException {
        try {
            return userDao.checkPhoneNum(phoneNumber);
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 회원가입 시 사용자 존재 여부 확인
    public int checkPassword(String email, String encryptPwd) throws BaseException  {
        try {
            return userDao.checkPassword(email, encryptPwd);
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 사용자 존재 여부 확인
    public int checkUser(int userIdx) throws BaseException {
        try {
            return userDao.checkUser(userIdx);
        } catch (Exception exception){
            System.out.println("checkUser"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 주소 존재 여부 확인
    public int checkOtherAddress(int otherIdx) throws BaseException  {
        try {
            return userDao.checkOtherAddress(otherIdx);
        } catch (Exception exception){
            System.out.println("checkUser"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 주소의 소유자 확인
    public int checkAddressUserCorrect(int userIdx, int addressIdx) throws BaseException {
        try {
            return userDao.checkAddressUserCorrect(userIdx, addressIdx);
        } catch (Exception exception){
            System.out.println("checkUser"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 가입된 회원 확인 - 이메일
    public int checkUserByEmail(String email) throws BaseException  {
        try {
            return userDao.checkUserByEmail(email);
        } catch (Exception exception){
            System.out.println("checkUserByEmail"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 가입된 회원 획인 - 휴대폰 번호
    public int checkUserByPhone(String phoneNumber) throws BaseException {
        try {
            return userDao.checkUserByPhone(phoneNumber);
        } catch (Exception exception){
            System.out.println("checkUserByPhone"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }
    // 휴대폰 번호로 가입된 이메일 가져오기
    public String getUserEmailByPhone(String phoneNumber) throws BaseException {
        try {
            return userDao.getUserEmailByPhone(phoneNumber);
        } catch (Exception exception){
            System.out.println("getUserEmailByPhone"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 사용자 현재 주소 가져오기
    public UserNowAddressInfo getUserNowInfo(int userIdx) throws BaseException {
        try {
            UserNowAddressInfo userNowAddressInfo = userDao.getUserNowInfo(userIdx);
            return userNowAddressInfo;
        } catch (Exception exception){
            System.out.println("getUserNowInfo"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 현재 주소 타입 확인
    public String checkNowAddressType(int userIdx) throws BaseException  {
        try {
            return userDao.checkNowAddressType(userIdx);
        } catch (Exception exception){
            System.out.println("checkNowAddressType"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // addressIdx 확인
    public int checkAddressNowIdx(int userIdx, String addressType) throws BaseException {
        try {
            return userDao.checkAddressNowIdx(userIdx, addressType);
        } catch (Exception exception){
            System.out.println("checkAddressNowIdx"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 사용자 쿠폰 확인
    public int checkUserCoupon(int userIdx, int couponIdx) throws BaseException {
        try {
            return userDao.checkUserCoupon(userIdx, couponIdx);
        } catch (Exception exception){
            System.out.println("checkUserCoupon"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // addressIdx 존재 확인
    public int checkUserAddress(int addressIdx) throws BaseException {
        try {
            return userDao.checkUserAddress(addressIdx);
        } catch (Exception exception){
            System.out.println("checkUserAddress"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
