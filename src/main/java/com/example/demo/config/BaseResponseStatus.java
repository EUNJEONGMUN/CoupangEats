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
    // users
    USERS_EMPTY_USER_ID(false, 2010, "유저 아이디 값을 확인해주세요."),

    // [POST] /users
    POST_USERS_EMPTY_EMAIL(false, 2015, "이메일을 입력하세요."),
    POST_USERS_INVALID_EMAIL(false, 2016, "이메일을 올바르게 입력해주세요."),
    POST_USERS_EXISTS_EMAIL(false,2017,"이미 가입된 이메일 주소입니다. 다른 이메일을 입력하여 주세요."),
    PWD_CONTAINS_EMAIL(false, 2018, "아이디(이메일) 제외"),
    POST_USERS_INVALID_PWD(false, 2019, "영문/숫자/특수문자 2가지 이상 조합"),
    POST_SIGN_IN_INVALID_EMAIL(false, 2020, "아이디는 이메일 주소 형식으로 입력해주세요."),

    /**
     * 3000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),

    // [POST] /users
    DUPLICATED_EMAIL(false, 3013, "중복된 이메일입니다."),
    FAILED_TO_LOGIN(false,3014,"입력하신 아이디 또는 비밀번호가 일치하지 않습니다."),
    POST_USERS_EXISTS_PHONE(false, 3015, "이미 가입된 휴대폰 번호입니다."),


    EMPTY_STORE(false, 3100, "존재하지 않는 가게입니다."),

    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR(false, 4000, "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR(false, 4001, "서버와의 연결에 실패하였습니다."),

    //[PATCH] /users/{userIdx}
    MODIFY_FAIL_USERNAME(false,4014,"유저네임 수정 실패"),

    PASSWORD_ENCRYPTION_ERROR(false, 4011, "비밀번호 암호화에 실패하였습니다."),
    PASSWORD_DECRYPTION_ERROR(false, 4012, "비밀번호 복호화에 실패하였습니다."),

    EMPTY_STOREIDX_PARAM(false, 4100, "storeIdx를 입력하지 않았습니다.");

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
