package com.example.demo.src.user;

import com.example.demo.config.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.demo.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class UserProvider {
    private final UserDao userDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UserProvider(UserDao userDao) {
        this.userDao = userDao;
    }


    // 이메일 중복 확인
    public int checkEmail(String email) throws BaseException {
        try {
            return userDao.checkEmail(email);
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }

    }

    // 휴대폰 번호 중복 확인
    public int checkPhoneNum(String phoneNumber) throws BaseException {
        try {
            return userDao.checkPhoneNum(phoneNumber);
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 사용자 존재 여부 확인
    public int checkPassward(String email, String encryptPwd) throws BaseException  {
        try {
            return userDao.checkPassward(email, encryptPwd);
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
