package com.example.demo.src.user;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.user.model.Req.PostAddressReq;
import com.example.demo.src.user.model.Req.*;
import com.example.demo.src.user.model.Res.*;
import com.example.demo.src.user.model.UserNowAddressIdx;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;

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

    public UserController(UserProvider userProvider, UserService userService, JwtService jwtService) {
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * 회원 가입 API
     * [POST] /users/sign-up
     *
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
            if (userProvider.checkUserByEmail(postUserReq.getEmail()) != 0){
                return new BaseResponse<>(DUPLICATED_EMAIL);
            }
        }

        if (postUserReq.getPhoneNumber()!=null){
            // 휴대폰 번호 정규식 확인
            if (!isRegexPhone(postUserReq.getPhoneNumber())) {
                return new BaseResponse<>(POST_USERS_INVALID_PHONE);
            }
            if (userProvider.checkUserByPhone(postUserReq.getPhoneNumber()) != 0){
                String duplicatedEmail = userProvider.getUserEmailByPhone(postUserReq.getPhoneNumber());
                return new BaseResponse<>(DUPLICATED_PHONE, duplicatedEmail+" 아이디(이메일)로 가입된 휴대폰 번호입니다.");
            }
        }


        // 비밀번호 정규식 확인
        if (!isRegexPwd(postUserReq.getPassword())) {
            return new BaseResponse<>(POST_USERS_INVALID_PWD);
        }

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

        if (postUserReq.getEmail()==null){
            return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
        }
        if (postUserReq.getPhoneNumber()==null){
            return new BaseResponse<>(POST_USERS_INVALID_PHONE);
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
            return new BaseResponse<>(EMPTY_OTHER_ADDRESS_IDX);
        }

        if (putAddressReq.getStatus()==null || putAddressReq.getStatus().equals("")){
            putAddressReq.setStatus("Y");
        }

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
            return new BaseResponse<>(EMPTY_OTHER_ADDRESS_IDX);
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


}
