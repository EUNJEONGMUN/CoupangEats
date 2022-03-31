package com.example.demo.src.kakao;

import com.example.demo.src.kakao.model.KakaoProfile;
import com.example.demo.src.kakao.model.KakaoUserInfo;
import com.example.demo.src.kakao.model.OAuthToken;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class KakaoOAuth {

//
//    public KakaoUserInfo getUserInfo(String authorizedCode){
//        // 1. 인가 코드 -> 엑세스 토큰
//        String accessToken = getAccessToken(authorizedCode);
//        // 2. 엑세스 토큰 -> 카카오 사용자 정보
//        KakaoUserInfo userInfo = getUserInfoByToken(accessToken);
//
//        return userInfo;
//    }

    public String getAccessToken(String authorizedCode){
        String accessToken = "";
        String refreshToken = "";


        // HttpHeader 오브젝트 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpBody 오브젝트 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", "08d5dd6fb5616b5500d65886ff47c7f2");
        params.add("redirect_uri", "http://localhost:9009/users/kakao/sign-in");
        params.add("code", authorizedCode);

        // HttpHeader와 HttpBody를 하나의 오브젝트에 담기
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(params, headers);

        // Http 요청하기 : POST 방식, response 변수의 응답
        ResponseEntity<String> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        OAuthToken oAuthToken = null;
        try {
            oAuthToken = objectMapper.readValue(response.getBody(),OAuthToken.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("kakao access Token >>> "+oAuthToken.getAccess_token());

        return oAuthToken.getAccess_token();

    }

    public KakaoUserInfo getUserInfoByToken(String accessToken){
        // HttpHeader 오브젝트 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Bearer "+accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpHeader와 HttpBody를 하나의 오브젝트에 담기
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> KakaoProfileRequest = new HttpEntity<>(headers);

        // Http 요청하기 : POST 방식, response 변수의 응답
        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                KakaoProfileRequest,
                String.class
        );

        System.out.println(">>>"+response.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoProfile kakaoProfile = null;
        try {
            kakaoProfile = objectMapper.readValue(response.getBody(),KakaoProfile.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        String email = kakaoProfile.getKakao_account().getEmail();
        String nickName = kakaoProfile.getProperties().getNickname();
        System.out.println("kakao email " + email);
        System.out.println("kakao nickName " + nickName);

        
        return new KakaoUserInfo(email,nickName);
    }







}




















