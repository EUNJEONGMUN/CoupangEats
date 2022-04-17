package com.example.demo.utils;


import com.example.demo.config.BaseException;
import com.example.demo.config.secret.Secret;
import com.example.demo.src.user.UserDao;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static com.example.demo.config.BaseResponseStatus.*;

@Service
public class JwtService {

    private final UserDao userDao;

    public JwtService(UserDao userDao){
        this.userDao = userDao;
    }

    /*
    JWT 생성
    @param userIdx
    @return String
     */
    public String createJwt(int userIdx) {
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam("type", "jwt")
                .claim("userIdx", userIdx)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis() + 1 * (1000 * 60 * 60 * 24)))
                .signWith(SignatureAlgorithm.HS256, Secret.JWT_SECRET_KEY)
                .compact();
    }

    public String createRefreshToken(int userIdx) {
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam("type", "RefreshToken")
                .claim("userIdx", userIdx)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis() + 1 * (1000 * 60 * 60 * 24 * 365)))
                .signWith(SignatureAlgorithm.HS256, Secret.JWT_SECRET_KEY)
                .compact();
    }

    /*
    Header에서 X-ACCESS-TOKEN 으로 JWT 추출
    @return String
     */
    public String getJwt() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("X-ACCESS-TOKEN");
    }

    /*
    JWT에서 userIdx 추출
    @return int
    @throws BaseException
     */
    public int getUserIdx() throws BaseException {
        //1. JWT 추출
        String accessToken = getJwt();
        if (accessToken == null || accessToken.length() == 0) {
            throw new BaseException(EMPTY_JWT);
        }

        // 로그아웃 된 토큰인지 확인
        if (userDao.checkSignOutList(accessToken) == 1){
            throw new BaseException(SIGN_OUT_TOKEN);
        }

        // 2. JWT parsing
        Jws<Claims> claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(Secret.JWT_SECRET_KEY)
                    .parseClaimsJws(accessToken);
        } catch (Exception ignored) {
            throw new BaseException(INVALID_JWT);
        }

        // 3. userIdx 추출
        return claims.getBody().get("userIdx", Integer.class);
    }

    public int getUserIdxToken(String accessToken) throws BaseException{
        if (accessToken == null || accessToken.length() == 0) {
            throw new BaseException(EMPTY_JWT);
        }
        // 로그아웃 된 토큰인지 확인
        if (userDao.checkSignOutList(accessToken) == 1){
            throw new BaseException(SIGN_OUT_TOKEN);
        }

        // 2. JWT parsing
        Jws<Claims> claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(Secret.JWT_SECRET_KEY)
                    .parseClaimsJws(accessToken);
        } catch (Exception ignored) {
            throw new BaseException(INVALID_REFRESH_TOKEN);
        }
        return claims.getBody().get("userIdx", Integer.class);
    }

    public int getUserIdxOption() throws BaseException {
        //1. JWT 추출
        String accessToken = getJwt();
        if (accessToken == null || accessToken.length() == 0) {
            return 0;
        }
        // 로그아웃 된 토큰인지 확인
        if (userDao.checkSignOutList(accessToken) == 1){
            throw new BaseException(SIGN_OUT_TOKEN);
        }

        // 2. JWT parsing
        Jws<Claims> claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(Secret.JWT_SECRET_KEY)
                    .parseClaimsJws(accessToken);
        } catch (Exception ignored) {
            throw new BaseException(INVALID_JWT);
        }

        // 3. userIdx 추출
        return claims.getBody().get("userIdx", Integer.class);
    }

    public boolean validateTokenExceptExpiration(String jwtToken) throws BaseException {

        Jws<Claims> claims;
        try {
//            if (isLoggedOut(jwtToken)) return false;
            claims = Jwts.parser()
                    .setSigningKey(Secret.JWT_SECRET_KEY)
                    .parseClaimsJws(jwtToken);
            return claims.getBody().getExpiration().before(new Date());
        } catch(ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // accesstoken 유효시간
    public Date getExpiration() throws BaseException {
        String accessToken = getJwt();
        if (accessToken == null || accessToken.length() == 0) {
            throw new BaseException(EMPTY_JWT);
        }

        Jws<Claims> claims;

        claims = Jwts.parser()
                .setSigningKey(Secret.JWT_SECRET_KEY)
                .parseClaimsJws(accessToken);

//        Long now = new Date().getTime(); // getTime은 Date 타입의 시간을 밀리초로 환산해 가져온다.
//        return (claims.getBody().getExpiration().getTime()-now);
        return claims.getBody().getExpiration();
    }

}
