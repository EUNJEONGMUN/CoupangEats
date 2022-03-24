package com.example.demo.src.user;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.user.model.Address;
import com.example.demo.src.user.model.Req.*;
import com.example.demo.src.user.model.Res.*;
import com.example.demo.src.user.model.UserLocationRes;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.example.demo.config.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.*;

@RestController
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
//    @UnAuth
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
        if (!isRegexPwd(postUserReq.getPassward())) {
            return new BaseResponse<>(POST_USERS_INVALID_PWD);
        }

        if (!isRegexPwdLen(postUserReq.getPassward())){
            return new BaseResponse<>(POST_USERS_INVALID_PWD_LEN);
        }

        // 3개 이상연속 되거나 동일한 문자/숫자 체크
        if (!isRegexPwdContinuous(postUserReq.getPassward()) || isRegexPwdThreeSame(postUserReq.getPassward())){
            return new BaseResponse<>(POST_USERS_INVALID_PWD_CONTINUOUS);
        }

        String userEmail = postUserReq.getEmail().substring(0, postUserReq.getEmail().lastIndexOf("@"));

        // 아이디(이메일)와 비밀번호 동일 여부 체크
        if (postUserReq.getPassward().contains(userEmail)) {
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
//    @UnAuth
    @ResponseBody
    @PostMapping("/users/sign-in")
    public BaseResponse<PostSignInRes> signIn(@Valid @RequestBody PostSignInReq postSignInReq) throws BaseException {

        // 이메일(아이디) 정규식 확인
        if (!isRegexEmail(postSignInReq.getEmail())) {
            return new BaseResponse<>(POST_SIGN_IN_INVALID_EMAIL);
        }

        PostSignInRes postSignInRes = userService.signIn(postSignInReq);
        return new BaseResponse<>(postSignInRes);

    }

    /**
     * 집, 회사, 기타 주소지 관리 API
     * [PUT] /users/address?otherIdx=
     * @return BaseResponse<UserLocationRes>
     */
    @ResponseBody
    @PutMapping("/address")
    public BaseResponse<UserLocationRes> putAddress(@RequestParam(required = false, defaultValue = "0") int otherIdx,
                                                    @Valid @RequestBody Address address) throws BaseException {

//        int userIdx = (int) request.getAttribute("userIdx");
        int userIdx= jwtService.getUserIdx();

        if (userProvider.checkUser(userIdx) == 0) {
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        if (address.getAddressType()==null){
            address.setAddressType("O");
        }

        if (address.getStatus()==null){
            address.setStatus("Y");
        }
        if (!(address.getAddressType().equals("H") || address.getAddressType().equals("C") || address.getAddressType().equals("O"))){
            return new BaseResponse<>(INVALID_STATUS);
        }
        if (address.getAddressType().equals("H")) {
            UserLocationRes userLocationRes = userService.putHomeAddress(userIdx, address);
            return new BaseResponse<>(userLocationRes);
        } else if (address.getAddressType().equals("C")) {
            UserLocationRes userLocationRes = userService.putCompanyAddress(userIdx, address);
            return new BaseResponse<>(userLocationRes);
        }

        // 주소 존재 여부 확인
        if (otherIdx == 0){
            return new BaseResponse<>(EMPTY_OTHER_ADDRESS_IDX);
        }
        if (userProvider.checkOtherAddress(otherIdx) == 0) {
            return new BaseResponse<>(ADDRESS_NOT_EXISTS);
        }

        // 주소의 소유자 확인
        if (userProvider.checkAddressUser(userIdx, otherIdx)==0){
            return new BaseResponse<>(INCONSISTENCY_ADDRESS_USER);
        }
        UserLocationRes userLocationRes = userService.putOtherAddress(userIdx, otherIdx, address);
        return new BaseResponse<>(userLocationRes);

    }

    
    /**
     * 기타 주소지 추가 API
     * [POST] /users/address/other
     * @return BaseResponse<UserLocationRes>
     */
    @ResponseBody
    @PostMapping("/address/other")
    public BaseResponse<UserLocationRes> postOtherAddress(@Valid @RequestBody PostAddressReq postAddressReq) throws BaseException {
//        int userIdx = (int) request.getAttribute("userIdx");
        int userIdx= jwtService.getUserIdx();
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        if (postAddressReq.getAddressType()==null){
            postAddressReq.setAddressType("O");
        }

        if (!(postAddressReq.getAddressType().equals("O"))){
            return new BaseResponse<>(INVALID_STATUS);
        }

        UserLocationRes userLocationRes = userService.postOtherAddress(userIdx, postAddressReq);
        return new BaseResponse<>(userLocationRes);
    }

    /**
     * 주소지 조회 API
     * [GET] /users/address
     * @return BaseResponse<GetUserAddressRes>
     */
    @ResponseBody
    @GetMapping("/address-list")
    public BaseResponse<GetUserAddressRes> getUserAddress() throws BaseException {
//        int userIdx = (int) request.getAttribute("userIdx");
        int userIdx= jwtService.getUserIdx();
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        GetUserAddressRes getUserAddressRes = userProvider.getUserAddress(userIdx);
        return new BaseResponse<>(getUserAddressRes);
    }

    /**
     * 주소지 설정 API
     * [PUT] /users/address/choice
     * @return BaseResponse<UserLocationRes>
     */
    @ResponseBody
    @PutMapping("/address/choice")
    public BaseResponse<UserLocationRes> putAddressChoice(@Valid @RequestBody PutAddressChoiceReq putAddressChoiceReq) throws BaseException {
//        int userIdx = (int) request.getAttribute("userIdx");
        int userIdx= jwtService.getUserIdx();
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        UserLocationRes userLocationRes = userService.putAddressChoice(userIdx, putAddressChoiceReq);
        return new BaseResponse<>(userLocationRes);

    }


}
