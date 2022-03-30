package com.example.demo.src.kakao.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


public class KakaoOAuth {

    public KakaoUserInfo getUserInfo(String authorizedCode){
        // 1. 인가 코드 -> 엑세스 토큰
        String accessToken = getAccessToken(authorizedCode);
        // 2. 엑세스 토큰 -> 카카오 사용자 정보
        KakaoUserInfo userInfo = getUserInfoByToken(accessToken);

        return userInfo;
    }

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
        params.add("redirect_uri", "http://localhost:9009/kakao/sign-in");
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

        JSONParser jsonParser = new JSONParser();
        try {
            JsonElement element = (JsonElement) jsonParser.parse(response.getBody().toString());
            accessToken = element.getAsJsonObject().get("access_token").getAsString();
            refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return accessToken;

    }

    public KakaoUserInfo getUserInfoByToken(String accessToken){
        // HttpHeader 오브젝트 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Bearer"+accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpHeader와 HttpBody를 하나의 오브젝트에 담기
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> KakaoProfileRequest = new HttpEntity<>(headers);

        // Http 요청하기 : POST 방식, response 변수의 응답
        ResponseEntity<String> response = restTemplate.exchange(
                "https://kauth.kakao.com/v2/user/me",
                HttpMethod.POST,
                KakaoProfileRequest,
                String.class
        );

        JSONParser jsonParser = new JSONParser();
        String nickname="";
        String email="";
        try {
            JsonElement element = (JsonElement) jsonParser.parse(response.getBody().toString());
            JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
            JsonObject kakaoAccount = element.getAsJsonObject().get("kakao_account").getAsJsonObject();

            nickname = properties.getAsJsonObject().get("nickname").getAsString();
            email = kakaoAccount.getAsJsonObject().get("email").getAsString();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new KakaoUserInfo(email, nickname);
    }
}




















