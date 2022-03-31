package com.example.demo.src.user;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.kakao.KakaoService;
import com.example.demo.src.kakao.model.KakaoUserInfo;
import com.example.demo.src.user.model.Req.PostAddressReq;
import com.example.demo.src.user.model.Req.*;
import com.example.demo.src.user.model.Res.*;
import com.example.demo.src.user.model.SignInUser;
import com.example.demo.src.user.model.User;
import com.example.demo.src.user.model.UserNowAddressIdx;
import com.example.demo.src.user.model.UserNowAddressInfo;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;
import java.util.Random;

import static com.example.demo.config.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.*;

@RestController
@CrossOrigin(origins = "http://localhost:9009")
@RequestMapping("/users")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtService jwtService;
    @Autowired
    private final KakaoService kakaoService;

    public UserController(UserProvider userProvider, UserService userService, JwtService jwtService,KakaoService kakaoService) {
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
        this.kakaoService = kakaoService;
    }

    /**
     * 회원 가입 API
     * [POST] /users/sign-up
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/sign-up")
    public BaseResponse<String> createUser(@Valid @RequestBody PostUserReq postUserReq) throws BaseException {


        if (postUserReq.getEmail()!=null){
            // 이메일(아이디) 정규식 확인
            if (!isRegexEmail(postUserReq.getEmail())) {
                return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
            }
            // 가입된 회원 확인 - 이메일
            if (userProvider.checkUserByEmail(postUserReq.getEmail()) != 0){
                return new BaseResponse<>(DUPLICATED_EMAIL);
            }
        }

        if (postUserReq.getPhoneNumber()!=null){
            // 휴대폰 번호 정규식 확인
            if (!isRegexPhone(postUserReq.getPhoneNumber())) {
                return new BaseResponse<>(POST_USERS_INVALID_PHONE);
            }
            // 가입된 회원 확인 - 휴대폰 번호
            if (userProvider.checkUserByPhone(postUserReq.getPhoneNumber()) != 0){
                String duplicatedEmail = userProvider.getUserEmailByPhone(postUserReq.getPhoneNumber());
                return new BaseResponse<>(DUPLICATED_PHONE, duplicatedEmail+" 아이디(이메일)로 가입된 휴대폰 번호입니다.");
            }
        }


        // 비밀번호 정규식 확인 - 문자+숫자/ 문자+특수문자 / 숫자+특수문자 / 문자+숫자+특수문자
        if (!isRegexPwd(postUserReq.getPassword())) {
            return new BaseResponse<>(POST_USERS_INVALID_PWD);
        }

        // 비밀번호 정규식 확인 - 길이
        if (!isRegexPwdLen(postUserReq.getPassword())){
            return new BaseResponse<>(POST_USERS_INVALID_PWD_LEN);
        }

        // 3개 이상연속 되거나 동일한 문자/숫자 체크
        if (!isRegexPwdContinuous(postUserReq.getPassword()) || isRegexPwdThreeSame(postUserReq.getPassword())){
            return new BaseResponse<>(POST_USERS_INVALID_PWD_CONTINUOUS);
        }

        String userEmail = postUserReq.getEmail().substring(0, postUserReq.getEmail().lastIndexOf("@"));

        // 아이디(이메일)와 비밀번호 동일 여부 체크
        if (postUserReq.getPassword().contains(userEmail)) {
            return new BaseResponse<>(PWD_CONTAINS_EMAIL);
        }

        // NULL 값 확인
        if (postUserReq.getEmail()==null){
            return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
        }
        if (postUserReq.getPhoneNumber()==null){
            return new BaseResponse<>(POST_USERS_EMPTY_PHONE);
        }
        if (postUserReq.getUserName()==null){
            return new BaseResponse<>(POST_USERS_EMPTY_NAME);
        }
        userService.createUser(postUserReq);
        String result = "";
        return new BaseResponse<>(result);

    }

    /**
     * 로그인 API
     * [POST] /sign-in
     *
     * @return BaseResponse<PostSignInRes>
     */
    @ResponseBody
    @PostMapping("/sign-in")
    public BaseResponse<PostSignInRes> signIn(@Valid @RequestBody PostSignInReq postSignInReq) throws BaseException {

        // 이메일(아이디) 정규식 확인
        if (!isRegexEmail(postSignInReq.getEmail())) {
            return new BaseResponse<>(POST_SIGN_IN_INVALID_EMAIL);
        }

        System.out.println("여기");
        PostSignInRes postSignInRes = userService.signIn(postSignInReq);
        return new BaseResponse<>(postSignInRes);

    }

    /**
     * 회원 탈퇴 API
     * [PATCH] /users/deletion
     *
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/deletion")
    public BaseResponse<String> deleteUser(@Valid @RequestBody PatchUserReq patchUserReq)throws BaseException {
        int userIdx= jwtService.getUserIdx();

        if (userProvider.checkUser(userIdx) == 0) {
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        userService.deleteUser(userIdx, patchUserReq);
        String result = "";
        return new BaseResponse<>(result);
    }


    /**
     * 회원 정보 확인 API
     * [POST] /users/check-information
     * @return BaseResponse<PostUserCheckRes>
     */
    @ResponseBody
    @PostMapping("/check-information")
    public BaseResponse<PostUserCheckRes> checkUser(@Valid @RequestBody PostUserCheckReq postUserCheckReq)throws BaseException {
        int userIdx= jwtService.getUserIdx();

        if (userProvider.checkUser(userIdx) == 0) {
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        PostUserCheckRes postUserCheckRes = new PostUserCheckRes();
        if (userService.checkPassword(postUserCheckReq.getEmail(), postUserCheckReq.getPassword())==0){
            return new BaseResponse<>(FAILED_TO_CHECK_USER);
        } else {
            postUserCheckRes.setCorrect(true);
        }

        return new BaseResponse<>(postUserCheckRes);
    }


    /**
     * 주소지 추가 API
     * [POST] /users/address
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/address")
    public BaseResponse<UserNowAddressIdx> createAddress(@Valid @RequestBody PostAddressReq postAddressReq) throws BaseException {

        int userIdx= jwtService.getUserIdx();

        if (userProvider.checkUser(userIdx) == 0) {
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        if (postAddressReq.getAddressType()==null || postAddressReq.getAddressType().equals("")){
            postAddressReq.setAddressType("O");
        }

        if (!(postAddressReq.getAddressType().equals("H") || postAddressReq.getAddressType().equals("C") || postAddressReq.getAddressType().equals("O"))){
            return new BaseResponse<>(INVALID_STATUS);
        }

        String result = "";

        if (postAddressReq.getAddressType().equals("H") || postAddressReq.getAddressType().equals("C")){
            // 현재 주소가 있을 때
            int duplicatedAddressIdx = userProvider.checkAddressNowIdx(userIdx, postAddressReq.getAddressType());
            if (duplicatedAddressIdx != 0){
                userService.deleteExistsAddress(duplicatedAddressIdx);
            }
        }
        UserNowAddressIdx userNowAddressIdx = userService.createAddress(userIdx, postAddressReq);
        return new BaseResponse<>(userNowAddressIdx);

    }



    /**
     * 주소지 조회 API
     * [GET] /users/address-list
     * @return BaseResponse<GetUserAddressRes>
     */
    @ResponseBody
    @GetMapping("/address-list")
    public BaseResponse<GetUserAddressRes> getUserAddress() throws BaseException {
        int userIdx= jwtService.getUserIdx();
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        GetUserAddressRes getUserAddressRes = userProvider.getUserAddress(userIdx);
        return new BaseResponse<>(getUserAddressRes);
    }

    /**
     * 주소지 수정 API
     * [PUT] /users/address/status?addressIdx=
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PutMapping("/address/status")
    public BaseResponse<String> modifyAddress(@RequestParam(required = false, defaultValue = "0") int addressIdx,
                                              @Valid @RequestBody PutAddressReq putAddressReq) throws BaseException  {

        int userIdx= jwtService.getUserIdx();
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        if (addressIdx==0){
            return new BaseResponse<>(EMPTY_ADDRESS_IDX);
        }

//        if (putAddressReq.getStatus()==null || putAddressReq.getStatus().equals("")){
//            putAddressReq.setStatus("Y");
//        }

        if (putAddressReq.getAddressType()==null || putAddressReq.getAddressType().equals("")){
            putAddressReq.setAddressType("O");
        }

        if (!(putAddressReq.getAddressType().equals("H") || putAddressReq.getAddressType().equals("C") || putAddressReq.getAddressType().equals("O"))){
            return new BaseResponse<>(INVALID_STATUS);
        }

        if (putAddressReq.getAddressType().equals("H") || putAddressReq.getAddressType().equals("C")){
            // 현재 주소가 있을 때
            int duplicatedAddressIdx = userProvider.checkAddressNowIdx(userIdx, putAddressReq.getAddressType());
            if (duplicatedAddressIdx != 0 && duplicatedAddressIdx!=addressIdx){
                userService.deleteExistsAddress(duplicatedAddressIdx);
            }
        }


        userService.modifyAddress(userIdx, addressIdx, putAddressReq);
        String result = "";
        return new BaseResponse<>(result);


    }

    /**
     * 주소지 삭제 API
     * [PATCH] /users/address/deletion
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/address/deletion")
    public BaseResponse<String> deleteAddress(@Valid @RequestBody PatchAddressReq patchAddressReq) throws BaseException {
        int userIdx= jwtService.getUserIdx();
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }
        if (userProvider.checkUserAddress(patchAddressReq.getAddressIdx()) == 0){
            return new BaseResponse<>(ADDRESS_NOT_EXISTS);
        }
        if (userProvider.checkAddressUserCorrect(userIdx, patchAddressReq.getAddressIdx())==0){
            return new BaseResponse<>(INCONSISTENCY_ADDRESS_USER);
        }

        userService.deleteAddress(patchAddressReq.getAddressIdx());
        String result = "";
        return new BaseResponse<>(result);



    }



    /**
     * 현재 주소지 변경 API
     * [PUT] /users/address/choice
     * /choice?addressIdx=
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PutMapping("/address/choice")
    public BaseResponse<UserNowAddressIdx> putAddressChoice(@RequestParam(required = false, defaultValue = "0") int addressIdx) throws BaseException {

        int userIdx= jwtService.getUserIdx();
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        if (addressIdx==0){
            return new BaseResponse<>(EMPTY_ADDRESS_IDX);
        }


        UserNowAddressIdx userNowAddressIdx = userService.putAddressChoice(userIdx, addressIdx);
        return new BaseResponse<>(userNowAddressIdx);


    }

    /**
     * 할인 쿠폰 조회 API
     * [GET] /users/coupons
     * @return BaseResponse<List<GetUserCouponListRes>>
     */
    @ResponseBody
    @GetMapping("/coupons-list")
    public BaseResponse<List<GetUserCouponListRes>> getUserCoupons() throws BaseException {
        int userIdx= jwtService.getUserIdx();
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        List<GetUserCouponListRes> getUserCouponListRes = userProvider.getUserCoupons(userIdx);
        return new BaseResponse<>(getUserCouponListRes);
    }

    /**
     * 할인 쿠폰 받기 API
     * [POST] /users/coupons
     * /coupons?couponIdx=?
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/coupons")
    public BaseResponse<String> createUserCoupon(@RequestParam(required = false, defaultValue = "0") int couponIdx) throws BaseException {

        int userIdx= jwtService.getUserIdx();
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        if (couponIdx==0){
            return new BaseResponse<>(EMPTY_COUPON_IDX_PARAMS);
        }

        if (userProvider.checkUserCoupon(userIdx, couponIdx) == 1){
            return new BaseResponse<>(DUPLICATED_COUPON);
        }
        userService.createUserCoupon(userIdx, couponIdx);
        String result = "";
        return new BaseResponse<>(result);
    }


    /**
     * 마이 이츠 조회 API
     * [GET] /users/my-eats
     * @return BaseResponse<GetMyEatsRes>
     */
    @ResponseBody
    @GetMapping("/my-eats")
    public BaseResponse<GetMyEatsRes> getMyEats()throws BaseException {
        int userIdx= jwtService.getUserIdx();
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        GetMyEatsRes getMyEatsRes = userProvider.getMyEats(userIdx);

        return new BaseResponse<>(getMyEatsRes);
    }



    /**
     * 휴대폰 인증 API
     * [POST] /users/message
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/message")
    public BaseResponse<String> messageUser(@Valid @RequestBody PostMessageReq postMessageReq) throws BaseException {

        String phoneNumber = postMessageReq.getPhoneNumber();

        if (postMessageReq.getPhoneNumber()!=null) {
            // 휴대폰 번호 정규식 확인
            if (!isRegexPhone(postMessageReq.getPhoneNumber())) {
                return new BaseResponse<>(POST_USERS_INVALID_PHONE);
            }
        }
        //  난수 생성
        Random rand  = new Random();
        String numStr = "";
        for(int i=0; i<6; i++) {
            String ran = Integer.toString(rand.nextInt(10));
            numStr+=ran;
        }

        System.out.println("수신자 번호 : " + phoneNumber);
        System.out.println("인증번호 : " + numStr);

        // 인증번호 발송
        userService.certifiedPhoneNumber(phoneNumber,numStr);
        // 인증번호 저장
        userService.certifiedPhoneNumberSave(phoneNumber,numStr);
        String result = "";
        return new BaseResponse<>(result);

    }

    /**
     * 휴대폰 인증번호 확인 API
     * [POST] /users/message/check
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/message/check")
    public BaseResponse<PostMessageCheckRes> messageCheck(@Valid @RequestBody PostMessageCheckReq postMessageCheckReq) throws BaseException {

        if (postMessageCheckReq.getPhoneNumber()!=null) {
            // 휴대폰 번호 정규식 확인
            if (!isRegexPhone(postMessageCheckReq.getPhoneNumber())) {
                return new BaseResponse<>(POST_USERS_INVALID_PHONE);
            }
        }

        if (userProvider.checkCertificationPhone(postMessageCheckReq.getPhoneNumber())==0){
            return new BaseResponse<>(EMPTY_CERTIFICATION_PHONE_NUMBER);
        }

        // 인증 번호 발송 시간과 현재 시간 차이 구하기
        int timeDiff = userProvider.checkCertificationTime(postMessageCheckReq.getPhoneNumber());
        if(timeDiff>=10000){ // 원래는 3분이지만, 개발 테스트의 편의를 위해 시간을 길게 설정
            return new BaseResponse<>(FAILED_TO_CERTIFICATION_TIME);
        }

        if (!(userProvider.checkCertificationNum(postMessageCheckReq.getPhoneNumber(), postMessageCheckReq.getCertificationNum()))){
            return new BaseResponse<>(FAILED_TO_CERTIFICATION);
        }
        return new BaseResponse<>(new PostMessageCheckRes(true));

    }

    /**
     * 카카오 로그인 API
     * [GET] /users/kakao/sign-in
     * @return BaseResponse<PostSignInRes>
     */
    @RequestMapping(value="/kakao/sign-in")
    public BaseResponse<PostKakaoSignInRes> kakaoSignIn(@RequestParam("code") String code) throws BaseException {
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

        return new BaseResponse<>(new PostKakaoSignInRes(userIdx,jwt, userNowAddressInfo));

    }

//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "access-token", required = true, dataType = "String", paramType = "header"),
//            @ApiImplicitParam(name = "REFRESH-TOKEN", value = "refresh-token", required = true, dataType = "String", paramType = "header")
//    })
    // 토큰이 만료되었을 때
    @ResponseBody
    @PostMapping("/sign-in/refresh")
    public BaseResponse<PostSignInRes> refreshToken(@RequestParam(value="X-ACCESS-TOKEN", required = false) String token,
                                                 @RequestParam(value="REFRESH-TOKEN", required = false) String refreshToken) throws BaseException {

        if(token==null){
            return new BaseResponse<>();
        }
        if(refreshToken==null){
            return new BaseResponse<>();
        }

        PostSignInRes postSignInRes = userService.refreshToken(token, refreshToken);

        return new BaseResponse<>(postSignInRes);
    }
}
