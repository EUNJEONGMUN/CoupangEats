package com.example.demo.src.user;

import com.example.demo.src.user.model.*;
import com.example.demo.src.user.model.Req.*;
import com.example.demo.src.user.model.Res.GetUserAddressRes;
import com.example.demo.src.user.model.Res.GetUserCouponListRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

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
        String UserInfoQuery = "INSERT INTO User (userName, email, password, phoneNumber) VALUES (?,?,?,?);";
        Object[] UserInfoParams = new Object[]{postUserReq.getUserName(), postUserReq.getEmail(), postUserReq.getPassword(), postUserReq.getPhoneNumber()};
        return this.jdbcTemplate.update(UserInfoQuery, UserInfoParams);

    }

    /**
     * 주소지 조회 API
     * [GET] /users/address
     * @return BaseResponse<GetUserAddressRes>
     */
    public GetUserAddressRes getUserAddress(int userIdx) {
        // 집주소, 회사주소
        String Query = "SELECT userAddressIdx, buildingName, address, addressDetail, addressGuide, addressTitle, addressLongitude, addressLatitude, addressType, isNowLocation \n" +
                "FROM UserAddress \n" +
                "WHERE userIdx=? AND addressType=? AND status='Y';";
        String AllQuery = "SELECT userAddressIdx, buildingName, address, addressDetail, addressGuide, addressTitle, addressLongitude, addressLatitude, addressType, isNowLocation \n" +
                "FROM UserAddress \n" +
                "WHERE userIdx=? AND status='Y';";
        String nowQuery = "SELECT userAddressIdx, buildingName, address, addressDetail, addressGuide, addressTitle, addressLongitude, addressLatitude, addressType, isNowLocation\n" +
                "                FROM UserAddress\n" +
                "                WHERE userIdx=? AND isNowLocation='Y' AND status='Y';";


        String checkQuery = "SELECT EXISTS(SELECT * FROM UserAddress WHERE userIdx=? AND addressType=? AND status='Y');";
        String nowCheckQuery = "SELECT EXISTS(SELECT * FROM UserAddress WHERE userIdx=? AND isNowLocation='Y' AND status='Y');";

        Object[] Params1 = new Object[]{userIdx, "H"};
        Object[] Params2 = new Object[]{userIdx, "C"};
        Object[] Params3 = new Object[]{userIdx, "O"};

        AddressInfo nowAddress = new AddressInfo();
        if (this.jdbcTemplate.queryForObject(nowCheckQuery, int.class, userIdx) != 0){
            nowAddress = this.jdbcTemplate.queryForObject(nowQuery,
                    (rs, rowNum) -> new AddressInfo(
                            rs.getInt("userAddressIdx"),
                            rs.getString("buildingName"),
                            rs.getString("address"),
                            rs.getString("addressDetail"),
                            rs.getString("addressGuide"),
                            rs.getDouble("addressLongitude"),
                            rs.getDouble("addressLatitude"),
                            rs.getString("addressTitle")
                    ), userIdx);
        }

        AddressInfo homeAddress = new AddressInfo();
        if (this.jdbcTemplate.queryForObject(checkQuery, int.class, Params1) != 0){
            homeAddress = this.jdbcTemplate.queryForObject(Query,
                    (rs, rowNum) -> new AddressInfo(
                            rs.getInt("userAddressIdx"),
                            rs.getString("buildingName"),
                            rs.getString("address"),
                            rs.getString("addressDetail"),
                            rs.getString("addressGuide"),
                            rs.getDouble("addressLongitude"),
                            rs.getDouble("addressLatitude"),
                            rs.getString("addressTitle")
                    ), Params1);
        }

        AddressInfo companyAddress = new AddressInfo();
        if (this.jdbcTemplate.queryForObject(checkQuery, int.class, Params2) != 0){
            companyAddress = this.jdbcTemplate.queryForObject(Query,
                    (rs, rowNum) -> new AddressInfo(
                            rs.getInt("userAddressIdx"),
                            rs.getString("buildingName"),
                            rs.getString("address"),
                            rs.getString("addressDetail"),
                            rs.getString("addressGuide"),
                            rs.getDouble("addressLongitude"),
                            rs.getDouble("addressLatitude"),
                            rs.getString("addressTitle")
                    ), Params2);
        }

        List<AddressInfo> otherAddress = this.jdbcTemplate.query(Query,
              (rs, rowNum) -> new AddressInfo(
                      rs.getInt("userAddressIdx"),
                      rs.getString("buildingName"),
                      rs.getString("address"),
                      rs.getString("addressDetail"),
                      rs.getString("addressGuide"),
                      rs.getDouble("addressLongitude"),
                      rs.getDouble("addressLatitude"),
                      rs.getString("addressTitle")
              ), Params3);


        return new GetUserAddressRes(nowAddress,homeAddress,companyAddress, otherAddress);
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
    public int checkPassword(String email, String encryptPwd) {
        String Query = "SELECT EXISTS(SELECT * FROM User WHERE User.email=? AND User.password=? AND User.status='Y');";
        Object[] Params = new Object[]{email, encryptPwd};

        return this.jdbcTemplate.queryForObject(Query,
                int.class,
                Params);
    }

    // 사용자 정보 가져오기
    public User getUserInfo(PostSignInReq postSignInReq) {
        String Query = "SELECT U.userIdx, U.userName, U.phoneNumber, UL.userLongitude, UL.userLatitude\n" +
                "FROM User U JOIN UserLocation UL on U.userIdx = UL.userIdx\n" +
                "WHERE U.email=? AND U.password=?;";
        Object[] Params = new Object[]{postSignInReq.getEmail(), postSignInReq.getPassword()};


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

    // 가입된 회원 확인 - 이메일
    public int checkUserByEmail(String email) {
        String Query = "SELECT EXISTS(SELECT * FROM User WHERE email=?);";
        return this.jdbcTemplate.queryForObject(Query,
                int.class,
                email);
    }

    // 휴대폰 번호로 가입된 이메일 가져오기
    public String getUserEmailByPhone(String phoneNumber) {
        String Query = "SELECT email FROM User WHERE phoneNumber=?;";
        return this.jdbcTemplate.queryForObject(Query,
                String.class,
                phoneNumber);
    }

    // 가입된 회원 획인 - 휴대폰 번호
    public int checkUserByPhone(String phoneNumber) {
        String Query = "SELECT EXISTS(SELECT * FROM User WHERE phoneNumber=?);";
        return this.jdbcTemplate.queryForObject(Query,
                int.class,
                phoneNumber);
    }











    // 사용자 현재 주소 가져오기
    public UserNowAddressInfo getUserNowInfo(int userIdx) {

        String isNowAddressQuery = "SELECT EXISTS(SELECT * FROM UserAddress WHERE userIdx=? AND isNowLocation='Y' AND status='Y');";
        int isNowAddress = this.jdbcTemplate.queryForObject(isNowAddressQuery, int.class, userIdx);
        if (isNowAddress == 0){
            return new UserNowAddressInfo();
        }

        String getNowAddressQuery = "SELECT userAddressIdx, buildingName, address, addressDetail, addressGuide, addressTitle, addressLongitude, addressLatitude, addressType\n" +
                "FROM UserAddress\n" +
                "WHERE userIdx=? AND isNowLocation='Y' AND status='Y';";

        return this.jdbcTemplate.queryForObject(getNowAddressQuery,
                (rs, rowNum) -> new UserNowAddressInfo(
                        rs.getInt("userAddressIdx"),
                        rs.getString("buildingName"),
                        rs.getString("address"),
                        rs.getString("addressDetail"),
                        rs.getString("addressGuide"),
                        rs.getString("addressTitle"),
                        rs.getDouble("addressLongitude"),
                        rs.getDouble("addressLatitude"),
                        rs.getString("addressType")
                ), userIdx);

    }


    // 현재 주소 타입 확인
    public String checkNowAddressType(int userIdx) {
        String isNowAddressQuery = "SELECT EXISTS(SELECT * FROM UserAddress WHERE userIdx=? AND isNowLocation='Y' AND status='Y');";
        int isNowAddress = this.jdbcTemplate.queryForObject(isNowAddressQuery, int.class, userIdx);
        if (isNowAddress == 0){
            return "N";
        }

        String nowAddressTypeQuery = "SELECT addressType FROM UserAddress WHERE userIdx=? AND isNowLocation='Y' AND status='Y';";
        return this.jdbcTemplate.queryForObject(nowAddressTypeQuery, String.class, userIdx);
    }

    // 같은 유형 주소 아이디 확인
    public int checkAddressNowIdx(int userIdx, String addressType) {
        String Query1 = "SELECT EXISTS (SELECT * FROM UserAddress WHERE userIdx=? AND addressType=? AND status='Y');";
        Object[] Params1 = new Object[]{userIdx, addressType};
        int isAddressIdx = this.jdbcTemplate.queryForObject(Query1, int.class, Params1);
        if (isAddressIdx == 0){
            return isAddressIdx;
        }

        String Query2 = "SELECT userAddressIdx FROM UserAddress WHERE userIdx=? AND addressType=? AND status='Y';";
        return this.jdbcTemplate.queryForObject(Query2, int.class, Params1);
    }

    // 기존 주소 삭제
    public int deleteExistsAddress(int duplicatedAddressIdx) {
        String Query = "UPDATE UserAddress SET status='N', isNowLocation='N' WHERE userAddressIdx=?;";
        return this.jdbcTemplate.update(Query, duplicatedAddressIdx);
    }

    /**
     * 주소지 추가 API
     * [POST] /users/address
     * @return BaseResponse<String>
     */
    public int createAddress(int userIdx, PostAddressReq postAddressReq) {
        String Query = "INSERT INTO UserAddress (userIdx, buildingName, address, addressDetail, addressGuide, addressTitle, addressLongitude, addressLatitude, addressType) VALUES (?,?,?,?,?,?,?,?,?);";
        Object[] Params = new Object[]{userIdx, postAddressReq.getBuildingName(), postAddressReq.getAddress(), postAddressReq.getAddressDetail(), postAddressReq.getAddressGuide(),
        postAddressReq.getAddressTitle(), postAddressReq.getAddressLongitude(), postAddressReq.getAddressLatitude(), postAddressReq.getAddressType()};
        this.jdbcTemplate.update(Query, Params);

        String lastInsertIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery,int.class);



    }

    /**
     * 주소지 수정 API
     * [PUT] /users/address
     * @return BaseResponse<String>
     */
    public int modifyAddress(int userIdx, int addressIdx, PutAddressReq putAddressReq) {
        String Query = "UPDATE UserAddress \n" +
                "SET buildingName=?, address=?, addressDetail=?, addressGuide=?, addressTitle=?, addressLongitude=?, addressLatitude=?, addressType=?, status=? \n" +
                "WHERE userIdx=? AND userAddressIdx=?;";
        Object[] Params = new Object[]{putAddressReq.getBuildingName(), putAddressReq.getAddress(), putAddressReq.getAddressDetail(),putAddressReq.getAddressGuide(),
                putAddressReq.getAddressTitle(), putAddressReq.getAddressLongitude(), putAddressReq.getAddressLatitude(), putAddressReq.getAddressType(), putAddressReq.getStatus(),
        userIdx, addressIdx};

        return this.jdbcTemplate.update(Query, Params);

}
    /**
     * 현재 주소지 변경 API
     * [PUT] /users/address/choice
     * /choice?addressIdx=
     * @return BaseResponse<String>
     */
    public UserNowAddressIdx putAddressChoice(int userIdx, int addressIdx) {
        System.out.println(">>주소지 변경 진입<<");
        String Query = "SELECT EXISTS(SELECT * FROM UserAddress WHERE userIdx=? AND isNowLocation='Y' AND status='Y');";
        int isNowLocationExists = this.jdbcTemplate.queryForObject(Query, int.class, userIdx);

        System.out.println(">>isNowLocationExists<<"+isNowLocationExists);

        if (isNowLocationExists != 0){
            String findPastLocationQuery = "SELECT userAddressIdx FROM UserAddress WHERE userIdx=? AND isNowLocation='Y' AND status='Y';";
            int findPastLocation = this.jdbcTemplate.queryForObject(findPastLocationQuery, int.class, userIdx);

            String DeletePastLocation = "UPDATE UserAddress SET isNowLocation='N' WHERE userAddressIdx=?;";
            this.jdbcTemplate.update(DeletePastLocation, findPastLocation);
        }

        String updateNowLocation = "UPDATE UserAddress SET isNowLocation='Y' WHERE userIdx=? AND userAddressIdx=?;";
        Object[] Params = new Object[]{userIdx, addressIdx};
        this.jdbcTemplate.update(updateNowLocation, Params);

        String getNowAddressQuery = "SELECT userAddressIdx, buildingName, address, addressDetail, addressGuide, addressTitle, addressLongitude, addressLatitude, addressType\n" +
                "FROM UserAddress\n" +
                "WHERE userAddressIdx=?;";

        return this.jdbcTemplate.queryForObject(getNowAddressQuery,
                (rs, rowNum) -> new UserNowAddressIdx(
                        rs.getInt("userAddressIdx"))
                , addressIdx);

    }

    /**
     * 할인 쿠폰 조회 API
     * [GET] /users/coupons
     * @return BaseResponse<List<GetUserCouponListRes>>
     */
    public List<GetUserCouponListRes> getUserCoupons(int userIdx) {
        String Query = "SELECT UC.userCouponIdx, C.storeIdx, C.couponTitle,\n" +
                "       CONCAT(FORMAT(discountPrice,0),'원') AS discountPrice,\n" +
                "       CONCAT(FORMAT(limitPrice,0),'원 이상 주문 시') AS limitPrice,\n" +
                "       CONCAT(DATE_FORMAT(endDate, '%m/%d'),' 까지') AS endDate, C.couponType\n" +
                "FROM UserCoupon UC JOIN Coupon C on UC.couponIdx = C.couponIdx\n" +
                "WHERE userIdx=? AND C.status='Y' AND UC.status='Y' AND DATEDIFF(C.endDate, CURRENT_DATE())>=0;";

        return this.jdbcTemplate.query(Query,
                (rs, rowNum) -> new GetUserCouponListRes(
                        rs.getInt("userCouponIdx"),
                        rs.getInt("storeIdx"),
                        rs.getString("couponTitle"),
                        rs.getString("discountPrice"),
                        rs.getString("limitPrice"),
                        rs.getString("endDate"),
                        rs.getString("couponType")
                ), userIdx);
    }
    /**
     * 할인 쿠폰 받기 API
     * [POST] /users/coupons
     * /coupons?couponIdx=?
     * @return BaseResponse<String>
     */
    public int createUserCoupon(int userIdx, int couponIdx) {
        String Query = "INSERT INTO UserCoupon (userIdx, couponIdx) VALUES (?,?);";
        return this.jdbcTemplate.update(Query, userIdx, couponIdx);

    }

    // 사용자 쿠폰 확인
    public int checkUserCoupon(int userIdx, int couponIdx) {
        String Query ="SELECT EXISTS(SELECT * FROM UserCoupon WHERE (status='Y' OR status='U') AND userIdx=? AND couponIdx=?);";
        return this.jdbcTemplate.queryForObject(Query, int.class, userIdx, couponIdx);
    }
}
