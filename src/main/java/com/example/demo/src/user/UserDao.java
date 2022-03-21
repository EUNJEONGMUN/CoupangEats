package com.example.demo.src.user;

import com.example.demo.src.user.model.Req.PostSignInReq;
import com.example.demo.src.user.model.Req.PostUserReq;
import com.example.demo.src.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class UserDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    /**
     * 회원 가입 API
     * [POST] /users/sign-up
     * @return BaseResponse<String>
     */
    public int createUser(PostUserReq postUserReq) {
        // 사용자 정보 insert
        String UserInfoQuery = "INSERT INTO User (userName, email, passward, phoneNumber) VALUES (?,?,?,?);";
        Object[] UserInfoParams = new Object[]{postUserReq.getUserName(), postUserReq.getEmail(), postUserReq.getPassward(), postUserReq.getPhoneNumber()};
        return this.jdbcTemplate.update(UserInfoQuery, UserInfoParams);
    }


    // 이메일 중복 확인
    public int checkEmail(String email) {
        String Query = "SELECT EXISTS(SELECT * FROM User WHERE User.email=?);";
        String Param = email;

        return this.jdbcTemplate.queryForObject(Query,
                int.class,
                Param);
    }

    // 휴대폰 번호 중복 확인
    public int checkPhoneNum(String phoneNumber) {
        String Query = "SELECT EXISTS(SELECT * FROM User WHERE User.phoneNumber=?);";
        String Param = phoneNumber;

        return this.jdbcTemplate.queryForObject(Query,
                int.class,
                Param);
    }

    // 사용자 존재 여부 확인
    public int checkPassward(String email, String encryptPwd) {
        String Query = "SELECT EXISTS(SELECT * FROM User WHERE User.email=? AND User.passward=? AND User.status='Y');";
        Object[] Params = new Object[]{email, encryptPwd};

        return this.jdbcTemplate.queryForObject(Query,
                int.class,
                Params);
    }

    // 사용자 정보 가져오기
    public User getUserInfo(PostSignInReq postSignInReq) {
        String Query = "SELECT U.userIdx, U.userName, U.phoneNumber, UL.userLongitude, UL.userLatitude\n" +
                "FROM User U JOIN UserLocation UL on U.userIdx = UL.userIdx\n" +
                "WHERE U.email=? AND U.passward=?;";
        Object[] Params = new Object[]{postSignInReq.getEmail(), postSignInReq.getPassward()};


        return this.jdbcTemplate.queryForObject(Query,
                (rs, rowNum) -> new User(
                        rs.getInt("userIdx"),
                        rs.getString("userName"),
                        rs.getString("phoneNumber"),
                        rs.getDouble("userLongitude"),
                        rs.getDouble("userLatitude")
                ),
                Params
        );

    }
}
