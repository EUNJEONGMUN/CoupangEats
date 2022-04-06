package com.example.demo.src.user;

import com.example.demo.src.user.model.Req.PostAddressReq;
import com.example.demo.src.user.model.Req.PostSignInReq;
import com.example.demo.src.user.model.Req.PostUserReq;
import com.example.demo.src.user.model.Req.PutAddressReq;
import com.example.demo.src.user.model.Res.GetMyEatsRes;
import com.example.demo.src.user.model.Res.GetUserAddressRes;
import com.example.demo.src.user.model.Res.GetUserCouponListRes;
import com.example.demo.src.user.model.User;
import com.example.demo.src.user.model.UserNowAddressIdx;
import com.example.demo.src.user.model.UserNowAddressInfo;

import java.util.List;

public interface UserDaoInterface {
    // 회원가입
    void createUser(PostUserReq postUserReq);

    // 회원탈퇴
    void deleteUser(int userIdx);

    // 주소 조회
    GetUserAddressRes getUserAddress(int userIdx);

    // 이메일 중복 확인
    int checkEmail(String email);

    // 휴대폰 번호 중복 확인
    int checkPhoneNum(String phoneNumber);

    // 회원가입 시 사용자 존재 여부 확인
    int checkPassword(String email, String encryptPwd);

    // 가입된 회원 확인 - 이메일
    int checkUserByEmail(String email);

    // 사용자 정보 가져오기
    User getUserInfo(PostSignInReq postSignInReq);

    // 사용자 정보 가져오기 -kakao
    User getUserInfoKakao(String email);

    // 사용자 존재 여부 확인
    int checkUser(int userIdx);

    // 주소 존재 여부 확인
    int checkOtherAddress(int otherIdx);

    // 주소의 소유자 확인
    int checkAddressUserCorrect(int userIdx, int addressIdx);

    // 휴대폰 번호로 가입된 이메일 가져오기
    String getUserEmailByPhone(String phoneNumber);

    // 가입된 회원 획인 - 휴대폰 번호
    int checkUserByPhone(String phoneNumber);

    // 사용자 현재 주소 가져오기
    UserNowAddressInfo getUserNowInfo(int userIdx);

    // 현재 주소 타입 확인
    String checkNowAddressType(int userIdx);

    // 같은 유형 주소 아이디 확인
    int checkAddressNowIdx(int userIdx, String addressType);

    // 기존 주소 삭제
    int deleteExistsAddress(int duplicatedAddressIdx);

    // 주소지 추가 API
    int createAddress(int userIdx, PostAddressReq postAddressReq);

    // 주소지 수정 API
    int modifyAddress(int userIdx, int addressIdx, PutAddressReq putAddressReq);

    // 현재 주소지 변경 API
    UserNowAddressIdx putAddressChoice(int userIdx, int addressIdx);

    // 주소지 삭제 API
    int deleteAddress(int addressIdx);

    // 할인 쿠폰 조회 API
    List<GetUserCouponListRes> getUserCoupons(int userIdx);

    // 할인 쿠폰 받기 API
    int createUserCoupon(int userIdx, int couponIdx);

    // 마이 이츠 조회 API
    GetMyEatsRes getMyEats(int userIdx);

    // 사용자 쿠폰 확인
    int checkUserCoupon(int userIdx, int couponIdx);

    // addressIdx 존재 확인
    int checkUserAddress(int addressIdx);

    // 휴대폰 인증 번호 저장
    int certifiedPhoneNumberSave(String phoneNumber, String numStr);

    // 인증한 휴대폰 번호 존재 확인
    int checkCertificationPhone(String phoneNumber);

    // 휴대폰 인증번호 확인 API - 인증 시간 확인
    int checkCertificationTime(String phoneNumber);

    // 휴대폰 인증번호 확인 API
    boolean checkCertificationNum(String phoneNumber, int certificationNum);

    // 리프레시 토큰 저장
    void saveUserRefreshToken(String refreshToken, int userIdx);

    // 리프레시 토큰 찾기
    String findRefreshToken(int userIdx);


}
