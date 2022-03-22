package com.example.demo.src.user;

import com.example.demo.src.user.model.Address;
import com.example.demo.src.user.model.Req.*;
import com.example.demo.src.user.model.User;
import com.example.demo.src.user.model.UserLocationRes;
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

    /**
     * 집, 회사, 기타 주소지 관리 API - 집
     * [PUT] /users/address?otherIdx=
     * @return BaseResponse<UserLocationRes>
     */
    public UserLocationRes putHomeAddress(int userIdx, Address address) {

        String Query1 = "UPDATE User SET homeAddress=?, homeDetail=?,  homeGuide=? WHERE userIdx=?;";
        Object[] Params1 = new Object[]{address.getAddress(), address.getAddressDetail(), address.getAddressGuide(), userIdx};

        String Query2 = "UPDATE UserLocation SET userLongitude=?, userLatitude=? WHERE userIdx=?;";
        Object[] Params2 = new Object[]{address.getUserLongitude(), address.getUserLatitude(), userIdx};

        Object[] NoParams1 = new Object[]{null,null,null,userIdx};
        Object[] NoParams2 = new Object[]{0.0,0.0,userIdx};

        if (address.getStatus().equals("N")){
            this.jdbcTemplate.update(Query1,NoParams1);
            this.jdbcTemplate.update(Query2,NoParams2);
            return new UserLocationRes(0.0,0.0);

        }

        this.jdbcTemplate.update(Query1,Params1);
        this.jdbcTemplate.update(Query2,Params2);
        return new UserLocationRes(address.getUserLongitude(), address.getUserLatitude());

    }

    /**
     * 집, 회사, 기타 주소지 관리 API - 회사
     * [PUT] /users/address?otherIdx=
     * @return BaseResponse<UserLocationRes>
     */
    public UserLocationRes putCompanyAddress(int userIdx, Address address) {
        String Query1 = "UPDATE User SET companyAddress=?, companyDetail=?,  companyGuide=? WHERE userIdx=?;";
        Object[] Params1 = new Object[]{address.getAddress(), address.getAddressDetail(), address.getAddressGuide(), userIdx};

        String Query2 = "UPDATE UserLocation SET userLongitude=?, userLatitude=? WHERE userIdx=?;";
        Object[] Params2 = new Object[]{address.getUserLongitude(), address.getUserLatitude(), userIdx};

        Object[] NoParams1 = new Object[]{null,null,null,userIdx};
        Object[] NoParams2 = new Object[]{0.0,0.0,userIdx};

        if (address.getStatus().equals("N")){
            this.jdbcTemplate.update(Query1,NoParams1);
            this.jdbcTemplate.update(Query2,NoParams2);
            return new UserLocationRes(0.0,0.0);

        }

        this.jdbcTemplate.update(Query1,Params1);
        this.jdbcTemplate.update(Query2,Params2);
        return new UserLocationRes(address.getUserLongitude(), address.getUserLatitude());

    }

    /**
     * 집, 회사, 기타 주소지 관리 API - 기타
     * [PUT] /users/address?otherIdx=
     * @return BaseResponse<UserLocationRes>
     */
    public UserLocationRes putOtherAddress(int userIdx, int otherIdx, Address address) {
        String Query1 = "UPDATE UserOtherAddress SET address=?, addressDetail=?, addressGuide=?, addressTitle=? , status=? WHERE userAddressIdx=? AND userIdx=?;";
        Object[] Params1 = new Object[]{address.getAddress(), address.getAddressDetail(), address.getAddressGuide(), address.getAddressTitle(), address.getStatus(), otherIdx, userIdx};
        this.jdbcTemplate.update(Query1,Params1);
        if (address.getStatus().equals("N")){
            return new UserLocationRes(0.0,0.0);
        }
        return new UserLocationRes(address.getUserLongitude(), address.getUserLatitude());

    }


    /**
     * 기타 주소지 추가 API
     * [POST] /users/address
     * @return BaseResponse<UserLocationRes>
     */
    public UserLocationRes postOtherAddress(int userIdx, PostAddressReq postAddressReq) {
        String Query1 = "INSERT INTO UserOtherAddress (userIdx, address, addressDetail, addressGuide, addressTitle) VALUES(?,?,?,?,?);";
        Object[] Params1 = new Object[]{userIdx, postAddressReq.getAddress(), postAddressReq.getAddressDetail(), postAddressReq.getAddressGuide(), postAddressReq.getAddressTitle()};

        String Query2 = "UPDATE UserLocation SET userLongitude=?, userLatitude=? WHERE userIdx=?;";
        Object[] Params2 = new Object[]{postAddressReq.getUserLongitude(), postAddressReq.getUserLatitude(), userIdx};

        this.jdbcTemplate.update(Query1,Params1);
        this.jdbcTemplate.update(Query2,Params2);

        return new UserLocationRes(postAddressReq.getUserLongitude(), postAddressReq.getUserLatitude());

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

    // 회원가입 시 사용자 존재 여부 확인
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

    // 사용자 존재 여부 확인
    public int checkUser(int userIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM User WHERE status='Y' AND userIdx=?);";
        int Param = userIdx;

        return this.jdbcTemplate.queryForObject(Query,
                int.class,
                Param);

    }


    // 주소 존재 여부 확인
    public int checkOtherAddress(int otherIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM UserOtherAddress WHERE status='Y' AND userAddressIdx=?);";
        int Param = otherIdx;
        return this.jdbcTemplate.queryForObject(Query,
                int.class,
                Param);


    }
    // 주소의 소유자 확인
    public int checkAddressUser(int userIdx, int otherIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM UserOtherAddress WHERE status='Y' AND userAddressIdx=? AND userIdx=?);";
        Object[] Params = new Object[]{otherIdx, userIdx};
        return this.jdbcTemplate.queryForObject(Query,
                int.class,
                Params);
    }
}
