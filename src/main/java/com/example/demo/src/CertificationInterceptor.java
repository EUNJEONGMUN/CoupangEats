package com.example.demo.src;

import com.example.demo.config.BaseException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.example.demo.utils.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class CertificationInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public CertificationInterceptor(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        boolean check = checkAnnotation(handler, UnAuth.class);
        if(check){
            return true;
        }
        try {
            //jwt에서 idx 추출.
            int userIdxByJwt = jwtService.getUserIdx();
            request.setAttribute("userIdx", userIdxByJwt);

        } catch(BaseException exception){
            String requestURI = request.getRequestURI();

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

        private boolean checkAnnotation(Object handler, Class clas){
        HandlerMethod handlerMethod=(HandlerMethod) handler;
        if(handlerMethod.getMethodAnnotation(clas)!=null){
            return true;
        }
        return false;
    }


}
