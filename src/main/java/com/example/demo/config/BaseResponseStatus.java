package com.example.demo.config;

import lombok.Getter;

/**
 * 에러 코드 관리
 */
@Getter
public enum BaseResponseStatus {
    /**
     * 1000 : 요청 성공
     */
    SUCCESS(true, 1000, "요청에 성공하였습니다."),


    /**
     * 2000 : Request 오류
     */
    // Common
    REQUEST_ERROR(false, 2000, "입력값을 확인해주세요."),
    EMPTY_JWT(false, 2001, "JWT를 입력해주세요."),
    INVALID_JWT(false, 2002, "유효하지 않은 JWT입니다."),
    INVALID_USER_JWT(false,2003,"권한이 없는 유저의 접근입니다."),
    DEFAULT_ERROR(false, 2004, "DEFAULT_ERROR"),
    NOT_BLANK(false, 2005, "NOT_BLANK"),
    NOT_EMPTY(false, 2006, "NOT_EMPTY"),
    PATTERN(false, 2007, "PATTERN"),
    MIN_VALUE(false, 2008, "MIN"),
    SIZE(false, 2009, "SIZE"),
    NOT_NULL(false, 2010, "NOT_NULL"),
    // users
    USERS_EMPTY_USER_ID(false, 2010, "유저 아이디 값을 확인해주세요."),

    // [POST] /users
    POST_USERS_EMPTY_EMAIL(false, 2015, "이메일을 입력하세요."),
    POST_USERS_INVALID_EMAIL(false, 2016, "이메일을 올바르게 입력해주세요."),
    POST_USERS_EXISTS_EMAIL(false,2017,"이미 가입된 이메일 주소입니다. 다른 이메일을 입력하여 주세요."),
    PWD_CONTAINS_EMAIL(false, 2018, "아이디(이메일) 제외"),
    POST_USERS_INVALID_PWD(false, 2019, "영문/숫자/특수문자 2가지 이상 조합"),
    POST_SIGN_IN_INVALID_EMAIL(false, 2020, "아이디는 이메일 주소 형식으로 입력해주세요."),
    POST_USERS_INVALID_PHONE(false, 2021, "휴대폰 번호를 정확하게 입력하세요."),
    DUPLICATED_PHONE(false,2022, "아이디(이메일)로 가입된 휴대폰 번호입니다."),
    POST_USERS_INVALID_PWD_CONTINUOUS(false, 2023, "3개 이상 연속되거나 동일한 문자/숫자 제외"),
    POST_USERS_INVALID_PWD_LEN(false, 2024, "8자~20자"),
    POST_USERS_EMPTY_NAME(false, 2025, "이름을 정확히 입력하세요."),
    PUT_ADDRESS_CHOICE_PARAM_EMPTY(false, 2026, "addressType을 입력해주세요."),
    EMPTY_COUPON_IDX_PARAMS(false, 2027, "couponIdx를 입력해주세요."),
    POST_USERS_EMPTY_PHONE(false, 2028, "휴대폰 번호를 입력해주세요."),

    INVALID_STATUS(false, 2029, "상태값을 확인해주세요."),

    // orders
    POST_CART_PARAM_EMPTY(false, 2100, "가게 idx 혹은 메뉴 idx를 입력해주세요."),
    PUT_CART_PARAM_EMPTY(false, 2101, "가게 idx 혹은 카트 idx를 입력해주세요."),
    EMPTY_STOREIDX_PARAM(false, 2200, "storeIdx를 입력하지 않았습니다."),
    EMPTY_MENUIDX_PARAM(false, 2201, "menuIdx를 입력하지 않았습니다."),
    EMPTY_OTHER_ADDRESS_IDX(false, 2202, "addressIdx를 입력하지 않았습니다."),
    EMPTY_POSITION_PARAM(false, 2203, "위도, 혹은 경도를 입력하지 않았습니다."),
    EMPTY_USER_ORDER_IDX_PARAM(false, 2204, "userOrderIdx를 입력하지 않았습니다."),
    PATCH_CART_PARAM_EMPTY(false, 2205, "카트 idx를 입력하지 않았습니다."),
    EMPTY_REVIEWIDX_PARAM(false, 2206, "리뷰 idx를 입력하지 않았습니다."),
    /**
     * 3000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),

    // [POST] /users
    DUPLICATED_EMAIL(false, 3013, "이미 가입된 이메일 주소입니다. 다른 이메일을 입력하여 주세요."),
    FAILED_TO_LOGIN(false,3014,"입력하신 아이디 또는 비밀번호가 일치하지 않습니다."),
    POST_USERS_EXISTS_PHONE(false, 3015, "이미 가입된 휴대폰 번호입니다."),
    USER_NOT_EXISTS(false, 3016, "존재하지 않는 사용자입니다."),
    ADDRESS_NOT_EXISTS(false, 3017, "주소가 존재하지 않습니다."),
    INCONSISTENCY_ADDRESS_USER(false, 3018, "해당 사용자의 주소가 아닙니다."),
    DUPLICATED_COUPON(false, 3019, "이미 받은 쿠폰입니다."),
    USER_NOW_ADDRESS_NOT_EXISTS(false, 3020, "주소를 설정해주세요."),


    EMPTY_STORE(false, 3100, "존재하지 않는 가게입니다."),
    EMPTY_MENU(false, 3101, "존재하지 않는 메뉴입니다."),
    INCONSISTENCY_STORE_OWNER(false, 3102, "해당 가게의 메뉴가 아닙니다."),
    EMPTY_STORE_CATEGORY(false, 3103, "존재하지 않는 카테고리입니다."),
    FAVORITE_STORE_ALREADY(false, 3104, "이미 즐겨찾기 한 가게입니다."),
    FAVORITE_STORE_NOT_ALREADY(false, 3105, "즐겨찾기 하지 않은 가게입니다."),
    CART_DUPLICATE_STORE(false, 3200, "같은 가게의 메뉴만 담을 수 있습니다."),
    EMPTY_CART_STORE(false, 3201, "카트에 담긴 가게가 없습니다."),
    CART_NOT_DUPLICATE_STORE(false, 3202, "가게가 없거나 같은 가게만 있습니다. 다른 API로 접근해주세요."),
    INCONSISTENCY_REVIEW_USER(false, 3203,"해당 사용자의 리뷰가 아닙니다."),
    EXPIRATION_OF_REVIEW_EDIT(false, 3204, "리뷰 수정 기간이 지났습니다."),
    EXPIRATION_OR_REVIEW(false,3205,"리뷰 작성 가능 기간이 지났습니다."),
    ALREADY_CART_STORE(false, 3206, "카트에 담긴 가게가 있습니다."),
    INCONSISTENCY_CART_USER(false, 3207, "해당 사용자의 카트가 아닙니다."),
    LIKED_REVIEW_ALREADY(false, 3208, "이미 도움돼요 혹은 도움되지 않아요 한 리뷰입니다."),
    EMPTY_REVIEWIDX(false, 3209, "존재하지 않는 리뷰입니다."),
    EMPTY_LIKED_REVIEW(false, 3210, "도움 돼요 혹은 도움 되지 않아요 한 기록이 없습니다."),

    USER_ORDER_NOT_EXISTS(false, 3300, "주문 내역이 존재하지 않습니다."),
    INCONSISTENCY_ORDER_USER(false, 3301, "해당 사용자의 주문 내역이 아닙니다."),
    REVIEW_NOT_EXISTS(false, 3302, "리뷰가 존재하지 않습니다."),
    REVIEW_ALREADY_EXISTS(false, 3303, "리뷰가 이미 존재하거나 삭제한 리뷰입니다."),

    INCONSISTENCY_STORE_STATE(false, 3400, "배달 완료 된 주문만 재주문 할 수 있습니다."),



    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR(false, 4000, "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR(false, 4001, "서버와의 연결에 실패하였습니다."),

    //[PATCH] /users/{userIdx}
    MODIFY_FAIL_USERNAME(false,4014,"유저네임 수정 실패"),

    PASSWORD_ENCRYPTION_ERROR(false, 4011, "비밀번호 암호화에 실패하였습니다."),
    PASSWORD_DECRYPTION_ERROR(false, 4012, "비밀번호 복호화에 실패하였습니다."),

    FAIL_DELETE_USER(false, 4013, "회원 탈퇴에 실패했습니다."),
    FAIL_SEND_MESSAGE(false, 4014, "휴대폰 인증번호 전송에 실패했습니다."),

    FAIL_CREATE_CART(false, 4300, "배달 카트 담기에 실패하였습니다."),
    FAIL_DELETE_CART_STORE(false, 4301, "기존 카트에 담긴 가게 삭제에 실패하였습니다."),

    FAIL_PUT_ADDRESS(false, 4401, "주소지 수정에 실패했습니다."),
    FAIL_POST_OTHER_ADDRESS(false, 4402, "주소지 추가에 실패했습니다."),
    FAIL_DELETE_EXISTS_ADDRESS(false, 4403, "기존 주소지 삭제에 실패했습니다."),
    FAIL_MODIFY_ADDRESS(false, 4404, "주소지 수정에 실패했습니다."),
    FAIL_MODIFY_CART(false, 4405, "카드 수정에 실패했습니다."),
    FAIL_CREATE_ORDER(false, 4406, "주문하기에 실패했습니다."),
    FAIL_DELETE_ORDER(false, 4407, "주문 취소에 실패했습니다."),
    FAIL_CREATE_REORDER(false, 4408, "재주문 하기에 실패했습니다."),
    FAIL_DELETE_CART(false, 4409, "카트 삭제에 실패했습니다."),
    FAIL_DELETE_ADDRESS(false, 4410, "주소지 삭제에 실패했습니다."),

    FAIL_POST_FAVORITE_STORE(false, 4500, "즐겨찾기 설정에 실패했습니다."),
    FAIL_PUT_FAVORITE_STORE(false, 4501, "즐거찾기 해제에 실패했습니다."),
    FAIL_CREATE_USER_COUPON(false, 4502, "쿠폰 받기에 실패했습니다."),
    FAIL_POST_REVIEW(false, 4503, "리뷰 작성에 실패했습니다."),
    FAIL_MODIFY_REVIEW(false, 4504, "리뷰 수정에 실패했습니다."),
    FAIL_DELETE_REVIEW(false, 4505, "리뷰 삭제에 실패했습니다."),
    FAIL_LIKED_REVIEW(false, 4506, "도움돼요 혹은 도움되지 않아요 등록에 실패했습니다."),
    FAIL_DELETE_EXISTS_LIKED_REVIEW(false, 4507, "기존 리뷰 도움기록 삭제에 실패했습니다.");

    // 5000 : 필요시 만들어서 쓰세요
    // 6000 : 필요시 만들어서 쓰세요


    private final boolean isSuccess;
    private final int code;
    private final String message;

    private BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}
