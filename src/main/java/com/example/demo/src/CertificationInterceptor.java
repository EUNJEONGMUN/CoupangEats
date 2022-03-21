package com.example.demo.src;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.example.demo.utils.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.example.demo.config.BaseResponseStatus.SERVER_ERROR;

public class CertificationInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public CertificationInterceptor(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Object handler : 핸들러 매핑이 찾은 컨트롤러 클래스 객체

        String requestURI = request.getRequestURI();

        if (requestURI.equals("/error")) {

            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");

            BaseResponse baseResponse = new BaseResponse(SERVER_ERROR);
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(baseResponse);
            response.getWriter().write(json);
            return false;
        }


        HandlerMethod handlerMethod = (HandlerMethod) handler;
        UnAuth unAuth = handlerMethod.getMethodAnnotation(UnAuth.class);


        if (unAuth != null) {
            return true;
        }
        try {
            //jwt에서 idx 추출.
            int userIdxByJwt = jwtService.getUserIdx();
            request.setAttribute("userIdx", userIdxByJwt); // 사용자의 idx 추출하여 request값 으로 설정

        } catch (BaseException exception) {
//                String requestURI = request.getRequestURI();

            Map<String, String> map = new HashMap<>();
            map.put("requestURI", "/users/sign-in?redirectURI=" + requestURI);
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);

            // writeValueAsString() : class instance 변수를 json string으로 변환
            // writerWithDefaultPrettyPrinter() : 보기 좋게 출력

            response.getWriter().write(json);
            return false;
        }

        return true;

    }
}
