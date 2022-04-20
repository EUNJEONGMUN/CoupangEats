package com.example.demo.src.store;

import com.example.demo.src.store.model.*;
import com.example.demo.src.store.model.Req.GetStoreHomeReq;
import com.example.demo.src.store.model.Req.PostReviewReq;
import com.example.demo.src.store.model.Req.PutReviewReq;
import com.example.demo.src.store.model.Res.*;
import com.example.demo.src.user.model.UserLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository
public class StoreDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 홈 화면 조회 - 가게정보리스트
     */
    public GetStoreHomeRes getStoreHome(int idx, UserLocation userLocation) {

        // 가게 기본 정보
        String StoreInfoQuery = "SELECT S.storeIdx, S.storeLogoUrl, S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount,\n" +
                "                       S.isToGo, S.isCoupon, S.status, S.minimumPrice, S.buildingName, S.storeAddress, S.storeAddressDetail, DATE_FORMAT(S.createdAt, '%Y-%m-%d %H:%I:%S') AS createdAt,\n" +
                "       ROUND(ST_DISTANCE_SPHERE(POINT(S.storeLongitude,S.storeLatitude), POINT(?,?))*0.001,1) AS distance,\n" +
                "       CASE WHEN DATEDIFF(CURRENT_DATE(), S.createdAt)>14 THEN 'N' ELSE 'Y' END AS isNewStore\n" +
                "                FROM Store S\n" +
                "                LEFT JOIN (\n" +
                "                    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "                    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx WHERE R.status='Y'\n" +
                "                    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "                WHERE S.status != 'N' AND S.storeIdx=?;";


        // 배달비 개수 -> 여러 개면 물결 표시 붙여야 함.
        String DeliveryFeeCount = "SELECT COUNT(*) as feeCount\n" +
                "FROM DeliveryFee D\n" +
                "WHERE D.storeIdx=? AND D.status='Y';";

        // 최소 배달비
        String DeliveryFeeQuery = "SELECT CASE\n" +
                "    WHEN IFNULL(MIN(deliveryFee),0) =0\n" +
                "        THEN '무료배달'\n" +
                "    ELSE CONCAT(FORMAT(IFNULL(MIN(deliveryFee),0),0),'원')\n" +
                "    END AS fee\n" +
                "        FROM DeliveryFee D\n" +
                "        WHERE D.storeIdx=? AND D.status='Y';";

        // 주문 횟수
        String orderCountQuery = "SELECT COUNT(UO.userOrderIdx) AS orderCount\n" +
                "FROM UserOrder UO\n" +
                "WHERE storeIdx=?\n" +
                "GROUP BY UO.storeIdx;";

        // 가게 최대 할인 쿠폰
        String StoreCouponQuery = "SELECT C.couponIdx, CONCAT(FORMAT(IFNULL(C.discountPrice,0),0),'원') AS maxDiscountPrice, IFNULL(C.couponType,'N') AS couponType\n" +
                "FROM Store S\n" +
                "LEFT JOIN (SELECT RankRow.storeIdx, RankRow.couponIdx, RankRow.discountPrice, RankRow.couponType\n" +
                "            FROM (SELECT*, RANK() OVER (PARTITION BY storeIdX ORDER BY discountPrice DESC, couponIdx ASC) AS a\n" +
                "                    FROM Coupon\n" +
                "                    WHERE status='Y' AND  DATEDIFF(endDate, CURRENT_DATE())>=0) AS RankRow\n" +
                "                    WHERE RankRow.a <= 1) C ON C.storeIdx = S.storeIdx\n" +
                "WHERE S.storeIdx = ?;";

        // 가게 대표 이미지 3개
        String StoreImageQuery = "SELECT RankRow.imageUrl\n" +
                "FROM (SELECT*, RANK() OVER (PARTITION BY SI.storeIdX ORDER BY SI.storeImageIdx) AS a\n" +
                "      FROM StoreImage SI\n" +
                "     ) AS RankRow\n" +
                "WHERE RankRow.a <= 3 AND RankRow.storeIdx=?;";

        Object[] Params = new Object[]{userLocation.getUserLongitude(), userLocation.getUserLatitude(), idx};

        // 가게 로고 이미지 -> 없으면 "N"
        String logo = this.jdbcTemplate.queryForObject("SELECT storeLogoUrl FROM Store WHERE status != 'N' AND storeIdx=?", String.class, idx);
        if (logo==null){
            logo = "N";
        }

        // 주문 횟수
        int oc = 0;
        if (this.jdbcTemplate.queryForObject("SELECT EXISTS(SELECT * FROM UserOrder UO WHERE storeIdx=?);", int.class, idx)!=0){
            oc = this.jdbcTemplate.queryForObject(orderCountQuery,
                    int.class,
                    idx);
        }
        int orderCount = oc;

        // 배달비 개수
        int deliveryFeeCount =  this.jdbcTemplate.queryForObject(DeliveryFeeCount,
                int.class,
                idx);

        // 최소 배달비
        String fee = this.jdbcTemplate.queryForObject(DeliveryFeeQuery,
                String.class,
                idx);

        // 배달비 조건이 여러 개 일 경우 배달비 뒤에 물결 표시
        if (deliveryFeeCount>1){
            fee = fee+"~";
        }
        String deliveryFee = fee;

        // 쿠폰 존재 여부 확인
        String isStoreCouponQuery = "SELECT EXISTS(SELECT * FROM Coupon WHERE status='Y' AND DATEDIFF(endDate, CURRENT_DATE())>=0 AND storeIdx=?)";

        String isCoupon = "Y";
        if (this.jdbcTemplate.queryForObject(isStoreCouponQuery, int.class, idx)==0){
            isCoupon = "N";
        }

        String finalIsCoupon = isCoupon;

        // 가게 영업 상태 -> 영업 종료일 경우 다음 영업시간
        String businessStatus = getBusinessHours(idx);

        String storeLogoUrl = logo;

        StoreInfo storeInfo = this.jdbcTemplate.queryForObject(StoreInfoQuery,
                (rs1, rowNum1) -> new StoreInfo(
                        rs1.getInt("storeIdx"),
                        this.jdbcTemplate.query(StoreImageQuery,
                                (rs2, rowNum2) -> new String(
                                        rs2.getString("imageUrl")),
                                rs1.getInt("storeIdx")),
                        storeLogoUrl,
                        rs1.getString("storeName"),
                        rs1.getString("isCheetah"),
                        rs1.getString("timeDelivery"),
                        rs1.getString("isToGo"),
                        finalIsCoupon,
                        rs1.getInt("minimumPrice"),
                        rs1.getString("buildingName"),
                        rs1.getString("storeAddress"),
                        rs1.getString("storeAddressDetail"),
                        rs1.getString("status"),
                        rs1.getString("createdAt"),
                        rs1.getDouble("reviewScore"),
                        rs1.getInt("reviewCount"),
                        rs1.getDouble("distance"),
                        orderCount,
                        deliveryFee,
                        rs1.getString("isNewStore"),
                        businessStatus
                ), Params);


        StoreBestCoupon storeBestCoupon = this.jdbcTemplate.queryForObject(StoreCouponQuery,
                (rs2, rowNum2) -> new StoreBestCoupon(
                        rs2.getInt("couponIdx"),
                        rs2.getString("maxDiscountPrice"),
                        rs2.getString("couponType")
                ), idx);

        return new GetStoreHomeRes(storeInfo, storeBestCoupon);
    }

    /**
     * 가게 영업시간
     */
    public String getBusinessHours(int storeIdx){
        String CheckQuery = "SELECT EXISTS(SELECT * FROM BusinessHours BH WHERE BH.status!='N' AND BH.storeIdx=? AND BH.opentime<=NOW() AND BH.closeTime>NOW() AND BH.day= DAYOFWEEK(NOW()));";
        String businessStatusQuery = "SELECT CASE WHEN status='Y' THEN '영업중'\n" +
                "    WHEN status='P' THEN '준비중'\n" +
                "ELSE '임시 휴무'\n" +
                "END AS status\n" +
                "FROM Store WHERE storeIdx=? AND status!='N';";

        // 프로시저 지정
        SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("business_hours_loop");

        // 프로시저 호출
        Map simpleJdbcCallResult = simpleJdbcCall.execute(storeIdx);
        System.out.println(">>"+simpleJdbcCallResult);

        // 프로시저 호출 결과에서 nextBusinessHours 추출
        ArrayList arrayList = new ArrayList();
        arrayList = (ArrayList) simpleJdbcCallResult.get("#result-set-1");
        Map resultMap = (Map) arrayList.get(0);

        System.out.println("nextBusinessHours: " + resultMap.get("nextBusinessHours"));
        // nextBusinessHours에 저장
        String nextBusinessHours = (String) resultMap.get("nextBusinessHours");

        int result = this.jdbcTemplate.queryForObject(CheckQuery, int.class, storeIdx);
        if (result == 1){
            return this.jdbcTemplate.queryForObject(businessStatusQuery, String.class, storeIdx);
        }
        else {
            return nextBusinessHours;
        }
    }


    /**
     * 가게 상세 화면 조회 API
     * [GET] /stores/detail?storeIdx=
     * @return BaseResponse<List<GetStoreHomeRes>>
     */
    public GetStoreDetailRes getStoreDetail(UserLocation userLocation, int storeIdx, int userIdx) {

        // 가게 정보
        String StoreInfoQuery = "SELECT S.storeIdx, S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount,\n" +
                "       CASE WHEN S.isToGo='Y' THEN S.timeToGo ELSE 'N' END AS timeToGo,\n" +
                "       S.isToGo, S.isCoupon, S.status, S.minimumPrice, S.buildingName, S.storeAddress, S.storeAddressDetail, S.storeLongitude,S.storeLatitude,\n" +
                "       ROUND(ST_DISTANCE_SPHERE(POINT(S.storeLongitude,S.storeLatitude), POINT(?,?))*0.001,1) AS distance\n" +
                "                FROM Store S\n" +
                "                LEFT JOIN (\n" +
                "                    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "                    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx WHERE R.status='Y'\n" +
                "                    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "                WHERE S.status != 'N' AND S.storeIdx=?;";

        // 가게 쿠폰 리스트
        String StoreCouponQuery = "SELECT couponIdx, couponTitle, CONCAT(FORMAT(discountPrice,0),'원') AS discountPrice, \n" +
                "       CONCAT(FORMAT(limitPrice,0),'원 이상 주문 시') AS limitPrice,\n" +
                "       CONCAT(DATE_FORMAT(endDate, '%m/%d'),' 까지') AS endDate, couponType\n" +
                "FROM Coupon WHERE status='Y' AND storeIdx=? AND DATEDIFF(endDate, CURRENT_DATE())>=0;";

        // 최소주문 금액
        String MinimumDeliveryFeeQuery = "SELECT IFNULL(MIN(deliveryFee),0) as fee\n" +
                "        FROM DeliveryFee D\n" +
                "        WHERE D.storeIdx=? AND D.status='Y';";

        // 배달비 정보
        String DeliveryFeeInfo = "SELECT storeIdx,\n" +
                "       CASE WHEN deliveryFee=0 THEN '무료' ELSE CONCAT(FORMAT(deliveryFee,0),'원') END AS deliveryFee,\n" +
                "       CASE WHEN maxPrice IS NULL THEN CONCAT(FORMAT(minPrice,0),'원 ~ ') ELSE CONCAT(FORMAT(minPrice,0), '원 ~ ', FORMAT(maxPrice,0),'원') END AS orderPrice\n" +
                "FROM DeliveryFee WHERE storeIdx=?;";

        // 메뉴 카테고리
        String MenuCategoryQuery = "SELECT MC.menuCategoryIdx, storeIdx, categoryName\n" +
                "FROM MenuCategory MC\n" +
                "WHERE MC.storeIdx=? AND MC.status='Y';";

        // 메뉴 상세 정보
        String MenuDetailQuery = "SELECT menuIdx, menuName, menuPrice, menuDetail, menuImgUrl, isOption,\n" +
                "       CASE\n" +
                "        WHEN status='T' THEN '오늘만 품절'\n" +
                "        WHEN status='S' THEN '품절'\n" +
                "        ELSE '판매중'\n" +
                "        END AS status\n" +
                "FROM Menu\n" +
                "WHERE status!='N' AND status != 'H' AND menuCategoryIdx=?;";

        // 메뉴 이미지 - 1개
        String MenuImageQuery = "SELECT RankRow.menuImgUrl\n" +
                "FROM (SELECT*, RANK() OVER (PARTITION BY M.menuIdx ORDER BY M.menuImgIdx) AS a\n" +
                "      FROM MenuImage M\n" +
                "     ) AS RankRow\n" +
                "WHERE RankRow.a <= 1 AND RankRow.menuIdx=?;";

        // 가게 리뷰가 3개 이상인지 확인 -> 3개 이상이어야 가게 상세 화면에서 보임
        String checkPhotoReviewThree = "SELECT EXISTS(SELECT S.storeIdx, PR.photoReviewCount\n" +
                "FROM Store S JOIN (\n" +
                "    SELECT UO.storeIdx, COUNT(storeIdx) AS photoReviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx = UO.userOrderIdx\n" +
                "    WHERE R.isPhoto='Y' AND R.status='Y' AND UO.status!='N'\n" +
                "    GROUP BY storeIdx) PR ON PR.storeIdx = S.storeIdx\n" +
                "WHERE S.status='Y' AND PR.photoReviewCount>=3 AND S.storeIdx=?);";

        // 포토 리뷰
        String PhotoReviewQuery = "SELECT PhotoReview.reviewIdx, PhotoReview.reviewImgUrl, PhotoReview.content, PhotoReview.score, DATE_FORMAT(PhotoReview.createdAt, '%Y-%m-%d %H:%I:%S') AS createdAt\n" +
                "FROM UserOrder UO\n" +
                "JOIN (SELECT R.reviewIdx,FirstPhoto.reviewImgUrl,R.content,R.score,R.userOrderIdx, R.createdAt\n" +
                "FROM Review R\n" +
                "JOIN (SELECT First.reviewIdx, First.reviewImgUrl\n" +
                "        FROM (SELECT*, RANK() OVER (PARTITION BY RI.reviewIdx ORDER BY RI.reviewImgIdx) AS a\n" +
                "              FROM ReviewImg RI\n" +
                "            WHERE RI.status='Y'\n" +
                "             ) AS First\n" +
                "        WHERE First.a <= 1) FirstPhoto ON FirstPhoto.reviewIdx = R.reviewIdx\n" +
                "      WHERE R.isPhoto='Y'\n" +
                "        AND R.status='Y') PhotoReview ON PhotoReview.userOrderIdx = UO.userOrderIdx\n" +
                "WHERE UO.status!='Y' AND UO.storeIdx=?;";

        // 가게 대표 이미지
        String StoreImageQuery = "SELECT RankRow.storeIdx, RankRow.imageUrl\n" +
                "FROM (SELECT*, RANK() OVER (PARTITION BY SI.storeIdX ORDER BY SI.storeImageIdx) AS a\n" +
                "      FROM StoreImage SI\n" +
                "     ) AS RankRow\n" +
                "WHERE RankRow.a <= 3 AND RankRow.storeIdx=?;";

        List<String> storeImgUrl = this.jdbcTemplate.query(StoreImageQuery,
                (rs1, rowNum1) -> new String(
                        rs1.getString("imageUrl")
                ),storeIdx);

        // 가게 총 주문량
        String StoreTotalOrderQuery = "SELECT ROUND(COUNT(*)*0.5) AS storeTotalOrderCount\n" +
                "FROM UserOrder\n" +
                "WHERE storeIdx=? AND (status!='E' AND status!='F');";
        int storeTotalOrder = this.jdbcTemplate.queryForObject(StoreTotalOrderQuery, int.class, storeIdx);
        // 가게의 총 메뉴 좋아요 수
        String StoreTotalMenuGood = "SELECT ROUND(COUNT(*)*0.5)\n" +
                "FROM CartToOrder CTO JOIN Cart C on CTO.cartIdx = C.cartIdx\n" +
                "WHERE C.storeIdx=? AND CTO.isGood='G';";
        int storeTotalMenuGood = this.jdbcTemplate.queryForObject(StoreTotalMenuGood, int.class, storeIdx);

        // 주문많음
        String IsManyOrderQuery = "SELECT CASE WHEN COUNT(*)>=? THEN 'Y' ELSE 'N' END AS isManyOrder\n" +
                "FROM CartToOrder CTO JOIN Cart C on CTO.cartIdx = C.cartIdx\n" +
                "WHERE C.menuIdx=? AND CTO.status!='N';";
        // 리뷰 최고
        String IsManyReviewQuery = "SELECT CASE WHEN COUNT(*)>=? THEN 'Y' ELSE 'N' END AS isManyReview\n" +
                "FROM CartToOrder CTO JOIN Cart C on CTO.cartIdx = C.cartIdx\n" +
                "WHERE C.menuIdx=? AND CTO.isGood='G' AND CTO.status!='N';";


        String isFavoriteStoreQuery = "SELECT EXISTS(SELECT * FROM Favorite WHERE storeIdx=? AND userIdx=? AND status='Y');";
        String isFavorite = "N";
        if (userIdx!=0){
            // 로그인 한 상태라면
            Object[] FavoriteParam = new Object[]{storeIdx, userIdx};
            if (this.jdbcTemplate.queryForObject(isFavoriteStoreQuery, int.class, FavoriteParam)==1){
                // 좋아요 한 가게라면
                isFavorite = "Y";
            }
        }
        String isFavoriteStore = isFavorite;


        int Param = storeIdx;
        Object[] StoreInfoParams = new Object[]{userLocation.getUserLongitude(),userLocation.getUserLatitude(), storeIdx};
        List<PhotoReview> photoReview = new ArrayList<>();
        if (this.jdbcTemplate.queryForObject(checkPhotoReviewThree, int.class, storeIdx)!=0) {
            photoReview = this.jdbcTemplate.query(PhotoReviewQuery,
                    (rs1, rowNum1) -> new PhotoReview(
                            rs1.getInt("reviewIdx"),
                            rs1.getString("reviewImgUrl"),
                            rs1.getString("content"),
                            rs1.getInt("score"),
                            rs1.getString("createdAt")
                    ), Param);
        }


        List<MenuCategory> menuCategory = this.jdbcTemplate.query(MenuCategoryQuery,
                (rs3, rowNum3) -> new MenuCategory(
                        rs3.getString("categoryName"),
                        this.jdbcTemplate.query(MenuDetailQuery,
                                (rs4, rowNum4) -> {
                                        int menuIdx = rs4.getInt("menuIdx");
                                        String menuName = rs4.getString("menuName");
                                        int menuPrice = rs4.getInt("menuPrice");
                                        String menuDetail = rs4.getString("menuDetail");
                                        if (menuDetail==null){
                                            menuDetail="";
                                        }
                                        String menuImgUrl = "";
                                        if (this.jdbcTemplate.queryForObject("SELECT EXISTS(SELECT * FROM MenuImage WHERE menuIdx=?);", int.class,rs4.getInt("menuIdx"))!=0){
                                            menuImgUrl = this.jdbcTemplate.queryForObject(MenuImageQuery, String.class, menuIdx);
                                        }
                                        String isOption = rs4.getString("isOption");
                                        String status = rs4.getString("status");
                                        String isManyOrder = this.jdbcTemplate.queryForObject(IsManyOrderQuery,
                                                String.class,
                                                storeTotalOrder, rs4.getInt("menuIdx"));
                                        String isManyReview = this.jdbcTemplate.queryForObject(IsManyReviewQuery,
                                                String.class,
                                                storeTotalMenuGood,rs4.getInt("menuIdx"));
                                        return new MenuDetail(menuIdx, menuName,menuPrice, menuDetail,menuImgUrl, isOption, status,isManyOrder, isManyReview);
                        }, rs3.getInt("menuCategoryIdx"))
                                    ), Param);



        int minimumDeliveryFee = this.jdbcTemplate.queryForObject(MinimumDeliveryFeeQuery, int.class, Param);
        List<DeliveryFeeInfo> deliveryFeeInfo = this.jdbcTemplate.query(DeliveryFeeInfo,
                (rs1, rowNum1) -> new DeliveryFeeInfo(
                        rs1.getInt("storeIdx"),
                        rs1.getString("orderPrice"),
                        rs1.getString("deliveryFee")
                ), Param);

        String UserOwnCoupon = "SELECT EXISTS(\n" +
                "SELECT C.couponIdx\n" +
                "FROM Coupon C\n" +
                "WHERE C.storeIdx=? AND C.couponIdx IN (SELECT couponIdx FROM UserCoupon UC WHERE UC.userIdx=?) AND C.couponIdx=?)";

        List<StoreCouponInfo> storeCouponInfo = this.jdbcTemplate.query(StoreCouponQuery,
                (rs2, rowNum2) -> new StoreCouponInfo(
                        rs2.getInt("couponIdx"),
                        rs2.getString("couponTitle"),
                        rs2.getString("discountPrice"),
                        rs2.getString("limitPrice"),
                        rs2.getString("endDate"),
                        rs2.getString("couponType"),
                        this.jdbcTemplate.queryForObject(UserOwnCoupon,
                                int.class, storeIdx, userIdx, rs2.getInt("couponIdx"))
                ), Param);


        List<PhotoReview> finalPhotoReview = photoReview;
        String isStoreCouponQuery = "SELECT EXISTS(SELECT * FROM Coupon WHERE status='Y' AND DATEDIFF(endDate, CURRENT_DATE())>=0 AND storeIdx=?)";
        String isCoupon = "Y";
        if (this.jdbcTemplate.queryForObject(isStoreCouponQuery, int.class, storeIdx)==0){
            isCoupon = "N";
        }

        String finalIsCoupon = isCoupon;
        return this.jdbcTemplate.queryForObject(StoreInfoQuery,
                (rs, rowNum) -> new GetStoreDetailRes(
                        rs.getInt("storeIdx"),
                        storeImgUrl,
                        rs.getString("storeName"),
                        rs.getString("isCheetah"),
                        rs.getString("timeDelivery"),
                        rs.getString("isToGo"),
                        finalIsCoupon,
                        rs.getInt("minimumPrice"),
                        rs.getString("buildingName"),
                        rs.getString("storeAddress"),
                        rs.getString("storeAddressDetail"),
                        rs.getDouble("storeLongitude"),
                        rs.getDouble("storeLatitude"),
                        rs.getDouble("distance"),
                        rs.getString("status"),
                        rs.getDouble("reviewScore"),
                        rs.getInt("reviewCount"),
                        rs.getString("timeToGo"),
                        storeCouponInfo,
                        minimumDeliveryFee,
                        deliveryFeeInfo,
                        finalPhotoReview,
                        menuCategory,
                        isFavoriteStore),
                StoreInfoParams);


    }

    /**
     * 메뉴 상세 화면 조회 조회 API
     * [GET] /stores/detail?storeIdx=&menuIdx=
     * @return BaseResponse<GetStoreMenuOptionsRes>
     */
    public GetStoreMenuOptionsRes getMenuOptions(int menuIdx) {

        String MenuInfoQuery = "SELECT M.menuIdx, M.menuImgUrl, M.menuName, M.menuDetail, M.menuPrice\n" +
                "FROM Menu M\n" +
                "WHERE M.menuIdx=?;";

        String MenuOptionsQuery = "SELECT O.optionsIdx, O.optionsTitle, O.isRequired, O.choiceCount\n" +
                "FROM MenuOptionsMapping MOM JOIN Options O on MOM.optionsIdx = O.optionsIdx\n" +
                "WHERE MOM.menuIdx =?;";

        String MenuOptionsDetailQuery ="SELECT optionsContent, addPrice\n" +
                "FROM OptionsDetail\n" +
                "WHERE optionsIdx=?;";

        String MenuImgQuery = "SELECT menuImgUrl FROM MenuImage WHERE menuIdx=?;";

        int Param = menuIdx;

        return this.jdbcTemplate.queryForObject(MenuInfoQuery,
                (rs1, rowNum1) -> new GetStoreMenuOptionsRes(
                        rs1.getInt("menuIdx"),
                        this.jdbcTemplate.query(MenuImgQuery,
                                (rs, rowNum) -> new String (
                                        rs.getString("menuImgUrl"))
                                , menuIdx),
                        rs1.getString("menuName"),
                        rs1.getString("menuDetail"),
                        rs1.getInt("menuPrice"),
                        this.jdbcTemplate.query(MenuOptionsQuery,
                                (rs2, rowNum2) -> new MenuOptions(
                                        rs2.getString("optionsTitle"),
                                        rs2.getString("isRequired"),
                                        rs2.getInt("choiceCount"),
                                        this.jdbcTemplate.query(MenuOptionsDetailQuery,
                                                (rs3, rowNum3) -> new MenuOptionsDetail(
                                                        rs3.getString("optionsContent"),
                                                        rs3.getInt("addPrice"))
                                                , rs2.getInt("optionsIdx"))
                                ), Param)
                ), Param);

    }

    /**
     * 즐겨찾기 등록 API
     * [POST] /stores/favorite?storeIdx=
     * /favorite?storeIdx=
     * @return BaseResponse<String>
     */
    public int createFavoriteStore(int userIdx, int storeIdx) {
        String Query = "INSERT INTO Favorite (userIdx, storeIdx) VALUES (?,?);";
        return this.jdbcTemplate.update(Query, userIdx, storeIdx);
    }

    /**
     * 즐겨찾기 해제 API
     * [PUT] /stores/favorite?storeIdx=
     * /favorite?storeIdx=
     * @return BaseResponse<String>
     */
    public int deleteFavoriteStore(int userIdx, String[] storeIdx) {
        String Query = "UPDATE Favorite SET status='N' WHERE userIdx=? AND storeIdx=? AND status='Y';";
        for (int i=0; i<storeIdx.length; i++){
            this.jdbcTemplate.update(Query, userIdx, storeIdx[i]);
        }
        return 1;
    }

    /**
     * 즐겨찾기 조회 API
     * [GET] /stores/favorite-list
     * @return BaseResponse<List<GetFavoriteListRes>>
     */
    public GetFavoriteList getFavoriteList(int userIdx, int storeIdx, UserLocation userLocation, String sort) {
        String StoreInfoQuery = "SELECT S.storeIdx, S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount,\n" +
                "       CASE WHEN S.isToGo='Y' THEN S.timeToGo ELSE 'N' END AS timeToGo,\n" +
                "       S.isToGo, S.isCoupon, S.status, S.minimumPrice, S.buildingName, S.storeAddress, S.storeAddressDetail,\n" +
                "       ROUND(ST_DISTANCE_SPHERE(POINT(S.storeLongitude,S.storeLatitude), POINT(?,?))*0.001,1) AS distance,\n" +
                "       CASE WHEN DATEDIFF(CURRENT_DATE(), S.createdAt)>14 THEN 'N' ELSE 'Y' END AS isNewStore\n" +
                "                FROM Store S\n" +
                "                LEFT JOIN (\n" +
                "                    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "                    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx WHERE R.status='Y'\n" +
                "                    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "                WHERE S.status != 'N' AND S.storeIdx=?;";

        String DeliveryFeeQuery = "SELECT CASE\n" +
                "    WHEN IFNULL(MIN(deliveryFee),0) =0\n" +
                "        THEN '무료배달'\n" +
                "    ELSE CONCAT(FORMAT(IFNULL(MIN(deliveryFee),0),0),'원')\n" +
                "    END AS fee\n" +
                "        FROM DeliveryFee D\n" +
                "        WHERE D.storeIdx=? AND D.status='Y';";


        String CouponQuery = "SELECT CASE WHEN couponType='D' THEN CONCAT(FORMAT(IFNULL(C.discountPrice,0),0),'원 배달쿠폰')\n" +
                "            WHEN couponType='T' THEN CONCAT(FORMAT(IFNULL(C.discountPrice,0),0),'원 포장쿠폰')\n" +
                "            WHEN couponType='B' THEN CONCAT(FORMAT(IFNULL(C.discountPrice,0),0),'원 배달·포장쿠폰')\n" +
                "            ELSE 'N' END AS coupon\n" +
                "                FROM Store S\n" +
                "                LEFT JOIN (SELECT RankRow.storeIdx, RankRow.couponIdx, RankRow.discountPrice, RankRow.couponType\n" +
                "                            FROM (SELECT*, RANK() OVER (PARTITION BY storeIdX ORDER BY discountPrice DESC, couponIdx ASC) AS a\n" +
                "                                    FROM Coupon\n" +
                "                                    WHERE status='Y' AND  DATEDIFF(endDate, CURRENT_DATE())>=0) AS RankRow\n" +
                "                                    WHERE RankRow.a <= 1) C ON C.storeIdx = S.storeIdx\n" +
                "                WHERE S.storeIdx = ?;";

        String StoreImageQuery = "SELECT RankRow.imageUrl\n" +
                "FROM (SELECT*, RANK() OVER (PARTITION BY SI.storeIdX ORDER BY SI.storeImageIdx) AS a\n" +
                "      FROM StoreImage SI\n" +
                "     ) AS RankRow\n" +
                "WHERE RankRow.a <= 1 AND RankRow.storeIdx=?;";
        String storeImgUrl = this.jdbcTemplate.queryForObject(StoreImageQuery, String.class, storeIdx);

        String businessStatus = getBusinessHours(storeIdx);


        String isStoreCouponQuery = "SELECT EXISTS(SELECT * FROM Coupon WHERE status='Y' AND DATEDIFF(endDate, CURRENT_DATE())>=0 AND storeIdx=?)";
        String isCoupon = "Y";
        if (this.jdbcTemplate.queryForObject(isStoreCouponQuery, int.class, storeIdx)==0){
            isCoupon = "N";
        }

        String finalIsCoupon = isCoupon;


        return this.jdbcTemplate.queryForObject(StoreInfoQuery,
                (rs1, rowNum)-> new GetFavoriteList(
                        rs1.getInt("storeIdx"),
                        storeImgUrl,
                        rs1.getString("storeName"),
                        rs1.getString("isCheetah"),
                        rs1.getString("timeDelivery"),
                        rs1.getString("isToGo"),
                        finalIsCoupon,
                        rs1.getDouble("distance"),
                        rs1.getString("status"),
                        rs1.getDouble("reviewScore"),
                        rs1.getInt("reviewCount"),
                        businessStatus,
                        this.jdbcTemplate.queryForObject(DeliveryFeeQuery,
                                String.class,
                                storeIdx),
                        this.jdbcTemplate.queryForObject(CouponQuery,
                                String.class,
                                storeIdx),
                        rs1.getString("isNewStore"))
                , userLocation.getUserLongitude(), userLocation.getUserLatitude(), storeIdx);

    }

    /**
     * 가게별 리뷰 조회 API
     * [GET] /stores/review-list
     * /review-list?storeIdx=
     * @return BaseResponse<List<GetStoreReviewListRes>>
     */
    public GetStoreReviewListRes getStoreReviews(int userIdx, int storeIdx, StoreReviewIdx idx) {

        // 리뷰 정보
        String ReviewInfoQuery = "SELECT R.reviewIdx, R.userIdx, R.userOrderIdx, R.score, R.content, R.isPhoto AS isPhotoReview,\n" +
                "       CASE\n" +
                "WHEN TIMESTAMPDIFF(DAY, R.createdAt, CURRENT_TIMESTAMP())<1\n" +
                "THEN '오늘'\n" +
                "WHEN TIMESTAMPDIFF(DAY, R.createdAt, CURRENT_TIMESTAMP())<7\n" +
                "THEN CONCAT(TIMESTAMPDIFF(DAY, R.createdAt, CURRENT_TIMESTAMP()), '일 전')\n" +
                "WHEN TIMESTAMPDIFF(WEEK, R.createdAt, CURRENT_TIMESTAMP())<2\n" +
                "THEN '지난 주'\n" +
                "WHEN TIMESTAMPDIFF(MONTH, R.createdAt, CURRENT_TIMESTAMP())<1\n" +
                "THEN '이번 달'\n" +
                "WHEN TIMESTAMPDIFF(MONTH, R.createdAt, CURRENT_TIMESTAMP())<2\n" +
                "THEN '지난 달'\n" +
                "ELSE DATE_FORMAT(R.createdAt, '%Y-%m-%d')\n" +
                "END AS uploadDate, DATE_FORMAT(R.createdAt, '%Y-%m-%d %H:%I:%S') AS createdAt, R.isPhoto\n" +
                "FROM Review R JOIN UserOrder UO on R.userOrderIdx = UO.userOrderIdx\n" +
                "WHERE R.reviewIdx=? AND R.status='Y';";

        String HelpedCountCheckQuery = "SELECT EXISTS(SELECT COUNT(RL.isHelped) AS helpedCount\n" +
                "                FROM ReviewLiked RL\n" +
                "                WHERE RL.isHelped='G' AND RL.reviewIdx=?\n" +
                "                GROUP BY RL.reviewIdx\n" +
                "                ORDER BY COUNT(RL.isHelped) DESC);";

        String HelpedCountQuery = "SELECT COUNT(RL.isHelped) AS helpedCount\n" +
                "FROM ReviewLiked RL\n" +
                "WHERE RL.isHelped='G' AND RL.reviewIdx=?\n" +
                "GROUP BY RL.reviewIdx\n" +
                "ORDER BY COUNT(RL.isHelped) DESC;";

        String BossReviewQuery = "SELECT reviewIdx, content,\n" +
                "       CASE\n" +
                "WHEN TIMESTAMPDIFF(DAY, createdAt, CURRENT_TIMESTAMP())<1\n" +
                "THEN '오늘'\n" +
                "WHEN TIMESTAMPDIFF(DAY, createdAt, CURRENT_TIMESTAMP())<7\n" +
                "THEN CONCAT(TIMESTAMPDIFF(DAY, createdAt, CURRENT_TIMESTAMP()), '일 전')\n" +
                "WHEN TIMESTAMPDIFF(WEEK, createdAt, CURRENT_TIMESTAMP())<2\n" +
                "THEN '지난 주'\n" +
                "WHEN TIMESTAMPDIFF(MONTH, createdAt, CURRENT_TIMESTAMP())<1\n" +
                "THEN '이번 달'\n" +
                "WHEN TIMESTAMPDIFF(MONTH, createdAt, CURRENT_TIMESTAMP())<2\n" +
                "THEN '지난 달'\n" +
                "ELSE DATE_FORMAT(createdAt, '%Y-%m-%d')\n" +
                "END AS bossUploadDate\n" +
                "FROM BossReview\n" +
                "WHERE reviewIdx=? AND status='Y';\n";

        String MenuQuery = "SELECT GROUP_CONCAT(DISTINCT (M.menuName) SEPARATOR ' · ') AS orderMenuListString\n" +
                "FROM Cart C JOIN (\n" +
                "    SELECT CTO.userIdx, CTO.cartIdx, UO.orderTime, CTO.isGood, UO.status\n" +
                "    FROM UserOrder UO JOIN CartToOrder CTO on UO.orderTime = CTO.orderTime\n" +
                "    WHERE UO.status!='N' AND UO.userOrderIdx=?\n" +
                "    ORDER BY UO.orderTime DESC) OrderMenu ON OrderMenu.cartIdx = C.cartIdx\n" +
                "JOIN Menu M on C.menuIdx = M.menuIdx;";

        String MyLikedQuery = "SELECT isHelped\n" +
                "FROM ReviewLiked\n" +
                "WHERE userIdx=? AND reviewIdx=? AND status='Y';";

        String MenuImgQuery = "SELECT reviewImgUrl\n" +
                "FROM ReviewImg\n" +
                "WHERE reviewIdx=? AND status='Y';";

        BossReview bossReview = new BossReview();
        if (this.jdbcTemplate.queryForObject("SELECT EXISTS(SELECT * FROM BossReview WHERE reviewIdx=?);", int.class, idx.getReviewIdx()) != 0){
            bossReview = this.jdbcTemplate.queryForObject(BossReviewQuery,
                    (rs, rowNum) -> new BossReview(
                            rs.getString("content"),
                            rs.getString("bossUploadDate")
                    ), idx.getReviewIdx());
        }

        List<String> reviewImg = this.jdbcTemplate.query(MenuImgQuery,
                (rs, rowNum) -> { return rs.getString("reviewImgUrl");
                }, idx.getReviewIdx());

        int helpedCount = 0;
        if (this.jdbcTemplate.queryForObject(HelpedCountCheckQuery, int.class, idx.getReviewIdx())!=0){
            helpedCount = this.jdbcTemplate.queryForObject(HelpedCountQuery,
                    int.class, idx.getReviewIdx());
        }
        // 로그인 했을 경우 좋아요 여부 확인
        String myHelped = "N";
        if (userIdx!=0){
            if (this.jdbcTemplate.queryForObject("SELECT EXISTS(SELECT status FROM ReviewLiked WHERE userIdx=? AND reviewIdx=? AND status='Y');", int.class, userIdx, idx.getReviewIdx()) != 0){
                myHelped = this.jdbcTemplate.queryForObject(MyLikedQuery,
                        String.class, userIdx, idx.getReviewIdx());
            }
        }
        String isMyHelped = myHelped;

        // 내가 쓴 리뷰인지 확인
        String myReview = "N";
        if (userIdx==idx.getUserIdx()){
            myReview = "Y";
        }
        String isMyReview = myReview;

        String userName = this.jdbcTemplate.queryForObject("SELECT userName FROM User WHERE userIdx=?;", String.class, idx.getUserIdx());

        String reviewUserName = userName.substring(0,1);

        for (int i=1; i<userName.length(); i++) {
            reviewUserName += "*";
        }

        BossReview finalBossReview = bossReview;
        String finalReviewUserName = reviewUserName;
        int finalHelpedCount = helpedCount;
        return this.jdbcTemplate.queryForObject(ReviewInfoQuery,
                (rs1, rowNum1) -> new GetStoreReviewListRes(
                        finalReviewUserName,
                        rs1.getInt("reviewIdx"),
                        rs1.getInt("score"),
                        rs1.getString("uploadDate"),
                        rs1.getString("createdAt"),
                        rs1.getString("content"),
                        this.jdbcTemplate.queryForObject(MenuQuery,
                                String.class, idx.getUserOrderIdx()),
                        reviewImg,
                        finalBossReview,
                        finalHelpedCount,
                        isMyHelped,
                        isMyReview,
                        rs1.getString("isPhotoReview")
                ), idx.getReviewIdx());

    }

    /**
     * 작성한 리뷰 조회 API
     * [GET] /stores/review?userOrderIdx=
     * /review?userOrderIdx=
     * @return BaseResponse<GetStoreReviewListRes>
     */
    public GetStoreMyReviewRes getStoreMyReview(int userIdx, int userOrderIdx) {

        String getReviewIdx = "SELECT reviewIdx\n" +
                "FROM UserOrder UO JOIN Review R on UO.userOrderIdx = R.userOrderIdx\n" +
                "WHERE UO.userOrderIdx=? AND R.status='Y';";

        int reviewIdx = this.jdbcTemplate.queryForObject(getReviewIdx, int.class, userOrderIdx);

        String ReviewInfoQuery = "SELECT R.reviewIdx, R.userIdx, R.userOrderIdx, R.score, R.content, R.isPhoto AS isPhotoReview,\n" +
                "       CASE\n" +
                "WHEN TIMESTAMPDIFF(DAY, R.createdAt, CURRENT_TIMESTAMP())<1\n" +
                "THEN '오늘'\n" +
                "WHEN TIMESTAMPDIFF(DAY, R.createdAt, CURRENT_TIMESTAMP())<7\n" +
                "THEN CONCAT(TIMESTAMPDIFF(DAY, R.createdAt, CURRENT_TIMESTAMP()), '일 전')\n" +
                "WHEN TIMESTAMPDIFF(WEEK, R.createdAt, CURRENT_TIMESTAMP())<2\n" +
                "THEN '지난 주'\n" +
                "WHEN TIMESTAMPDIFF(MONTH, R.createdAt, CURRENT_TIMESTAMP())<1\n" +
                "THEN '이번 달'\n" +
                "WHEN TIMESTAMPDIFF(MONTH, R.createdAt, CURRENT_TIMESTAMP())<2\n" +
                "THEN '지난 달'\n" +
                "ELSE DATE_FORMAT(R.createdAt, '%Y-%m-%d')\n" +
                "END AS uploadDate, DATE_FORMAT(R.createdAt, '%Y-%m-%d %H:%I:%S') AS createdAt, R.isPhoto\n" +
                "FROM Review R JOIN UserOrder UO on R.userOrderIdx = UO.userOrderIdx\n" +
                "WHERE R.reviewIdx=? AND R.status='Y';";

        String HelpedCountQuery = "SELECT COUNT(*) AS helpedCount\n" +
                "FROM ReviewLiked\n" +
                "WHERE reviewIdx=? AND isHelped='G' AND status='Y';";

        String BossReviewQuery = "SELECT reviewIdx, content,\n" +
                "       CASE\n" +
                "WHEN TIMESTAMPDIFF(DAY, createdAt, CURRENT_TIMESTAMP())<1\n" +
                "THEN '오늘'\n" +
                "WHEN TIMESTAMPDIFF(DAY, createdAt, CURRENT_TIMESTAMP())<7\n" +
                "THEN CONCAT(TIMESTAMPDIFF(DAY, createdAt, CURRENT_TIMESTAMP()), '일 전')\n" +
                "WHEN TIMESTAMPDIFF(WEEK, createdAt, CURRENT_TIMESTAMP())<2\n" +
                "THEN '지난 주'\n" +
                "WHEN TIMESTAMPDIFF(MONTH, createdAt, CURRENT_TIMESTAMP())<1\n" +
                "THEN '이번 달'\n" +
                "WHEN TIMESTAMPDIFF(MONTH, createdAt, CURRENT_TIMESTAMP())<2\n" +
                "THEN '지난 달'\n" +
                "ELSE DATE_FORMAT(createdAt, '%Y-%m-%d')\n" +
                "END AS bossUploadDate\n" +
                "FROM BossReview\n" +
                "WHERE reviewIdx=? AND status='Y';\n";

        String MenuQuery = "SELECT GROUP_CONCAT(DISTINCT (M.menuName) SEPARATOR ' · ') AS orderMenuListString\n" +
                "FROM Cart C JOIN (\n" +
                "    SELECT CTO.userIdx, CTO.cartIdx, UO.orderTime, CTO.isGood, UO.status\n" +
                "    FROM UserOrder UO JOIN CartToOrder CTO on UO.orderTime = CTO.orderTime\n" +
                "    WHERE UO.status!='N' AND UO.userOrderIdx=?\n" +
                "    ORDER BY UO.orderTime DESC) OrderMenu ON OrderMenu.cartIdx = C.cartIdx\n" +
                "JOIN Menu M on C.menuIdx = M.menuIdx;";

        String MenuImgQuery = "SELECT reviewImgUrl\n" +
                "FROM ReviewImg\n" +
                "WHERE reviewIdx=? AND status='Y';";

        BossReview bossReview = new BossReview();
        if (this.jdbcTemplate.queryForObject("SELECT EXISTS(SELECT * FROM BossReview WHERE reviewIdx=?);", int.class, reviewIdx) != 0){
            bossReview = this.jdbcTemplate.queryForObject(BossReviewQuery,
                    (rs, rowNum) -> new BossReview(
                            rs.getString("content"),
                            rs.getString("bossUploadDate")
                    ), reviewIdx);
        }


        List<String> reviewImg = this.jdbcTemplate.query(MenuImgQuery,
                (rs, rowNum) -> { return rs.getString("reviewImgUrl");
                }, reviewIdx);


        int helpedCount = this.jdbcTemplate.queryForObject(HelpedCountQuery,
                int.class, reviewIdx);



        BossReview finalBossReview = bossReview;
        return this.jdbcTemplate.queryForObject(ReviewInfoQuery,
                (rs1, rowNum1) -> new GetStoreMyReviewRes(
                        rs1.getInt("reviewIdx"),
                        rs1.getInt("score"),
                        rs1.getString("uploadDate"),
                        rs1.getString("createdAt"),
                        rs1.getString("content"),
                        this.jdbcTemplate.queryForObject(MenuQuery,
                                String.class, userOrderIdx),
                        reviewImg,
                        finalBossReview,
                        helpedCount
                ), reviewIdx);



    }

    /**
     * 리뷰 작성 API
     * [POST] /stores/review/new?userOrderIdx=
     * /new?userOrderIdx=
     * @return BaseResponse<String>
     */
    public int createReview(int userIdx, int userOrderIdx, PostReviewReq postReviewReq, List<String> imageList) {

        // 매장, 배달 모두 좋아요
        String InsertReviewInfoQuery1 = "INSERT INTO Review (userIdx, userOrderIdx, score, content, isPhoto,isDeliveryGood) VALUES(?,?,?,?,?,?);";

        // 매장만 불만
        String InsertReviewInfoQuery2 = "INSERT INTO Review (userIdx, userOrderIdx, score, content, isPhoto, reasonForStore, isDeliveryGood) VALUES(?,?,?,?,?,?,?);";

        // 배달만 불만
        String InsertReviewInfoQuery3 = "INSERT INTO Review (userIdx, userOrderIdx, score, content, isPhoto, isDeliveryGood, reasonForDelivery) VALUES(?,?,?,?,?,?,?);";

        // 둘 다 불만
        String InsertReviewInfoQuery4 = "INSERT INTO Review (userIdx, userOrderIdx, score, content, isPhoto, reasonForStore, isDeliveryGood, reasonForDelivery) VALUES(?,?,?,?,?,?,?,?);";


        String UpdateMenuIsGood = "UPDATE CartToOrder SET isGood=? WHERE userIdx=? AND cartIdx=?";
        String UpdateMenuReason = "UPDATE CartToOrder SET reasonForMenu=? WHERE userIdx=? AND cartIdx=?";

        String InsertImage = "INSERT INTO ReviewImg (reviewIdx, reviewImgUrl) VALUES (?,?);";

        String isPhoto;
        if (imageList.size() == 0) {
            // 포토리뷰가 아닐 경우
            isPhoto = "N";
        } else {
            isPhoto = "Y";
        }

        if (postReviewReq.getScore() > 2 && postReviewReq.getIsDeliveryManGood().equals("G")) {
            // 둘다 좋아요
            this.jdbcTemplate.update(InsertReviewInfoQuery1, userIdx, userOrderIdx, postReviewReq.getScore(),
                    postReviewReq.getContent(), isPhoto, postReviewReq.getIsDeliveryManGood());

        } else if (postReviewReq.getScore() <= 2 && postReviewReq.getIsDeliveryManGood().equals("G")) {
            // 매장만 불만
            this.jdbcTemplate.update(InsertReviewInfoQuery2, userIdx, userOrderIdx, postReviewReq.getScore(),
                    postReviewReq.getContent(), isPhoto, postReviewReq.getReasonForStore(), postReviewReq.getIsDeliveryManGood());

        } else if (postReviewReq.getScore() > 2 && postReviewReq.getIsDeliveryManGood().equals("B")){
            // 배달만 불만
            this.jdbcTemplate.update(InsertReviewInfoQuery3, userIdx, userOrderIdx, postReviewReq.getScore(),
                    postReviewReq.getContent(), isPhoto, postReviewReq.getIsDeliveryManGood(), postReviewReq.getReasonForDelivery());
        } else {
            // 다 불만
            this.jdbcTemplate.update(InsertReviewInfoQuery4, userIdx, userOrderIdx, postReviewReq.getScore(),
                    postReviewReq.getContent(), isPhoto, postReviewReq.getReasonForStore(), postReviewReq.getIsDeliveryManGood(), postReviewReq.getReasonForDelivery());

        }


        String lastInsertIdQuery = "select last_insert_id()";
        int reviewIdx = this.jdbcTemplate.queryForObject(lastInsertIdQuery,int.class);

        if (postReviewReq.getIsMenuGood().size()!=0){
            Set<Map.Entry<Integer,String>> entrySet = postReviewReq.getIsMenuGood().entrySet();
            Iterator<Map.Entry<Integer, String>> entryIterator = entrySet.iterator();

            while(entryIterator.hasNext()){
                Map.Entry<Integer, String> entry = entryIterator.next();
                Integer key = entry.getKey();
                String value = entry.getValue();
                this.jdbcTemplate.update(UpdateMenuIsGood, value, userIdx, key);
            }
        } else {
            Set<Map.Entry<Integer,String>> entrySet = postReviewReq.getReasonForMenu().entrySet();
            Iterator<Map.Entry<Integer, String>> entryIterator = entrySet.iterator();

            while(entryIterator.hasNext()){
                Map.Entry<Integer, String> entry = entryIterator.next();
                Integer key = entry.getKey();
                String value = entry.getValue();
                this.jdbcTemplate.update(UpdateMenuReason, value, userIdx, key);
            }
        }


        for (String url:imageList){
            this.jdbcTemplate.update(InsertImage, reviewIdx, url);
        }
        return 1;
    }


    /**
     * 리뷰 수정 API
     * [PUT] /stores/review?reviewIdx=
     * @return BaseResponse<String>
     */
    public int modifyReview(int userIdx, int reviewIdx, PutReviewReq putReviewReq, List<String> imageList) {
        // 매장, 배달 모두 좋아요
        String InsertReviewInfoQuery1 = "UPDATE Review SET score=?, content=?, isPhoto=?, isDeliveryGood=? WHERE reviewIdx=?;";

        // 매장만 불만
        String InsertReviewInfoQuery2 = "UPDATE Review SET score=?, content=?, isPhoto=?, reasonForStore=?, isDeliveryGood=? WHERE reviewIdx=?;";

        // 배달만 불만
        String InsertReviewInfoQuery3 = "UPDATE Review SET score=?, content=?, isPhoto=?, isDeliveryGood=?, reasonForDelivery=? WHERE reviewIdx=?;";

        // 둘 다 불만
        String InsertReviewInfoQuery4 = "UPDATE Review SET score=?, content=?, isPhoto=?, reasonForStore=?, isDeliveryGood=?, reasonForDelivery=? WHERE reviewIdx=?;";


        String UpdateMenuIsGood = "UPDATE CartToOrder SET isGood=? WHERE userIdx=? AND cartIdx=?";
        String UpdateMenuReason = "UPDATE CartToOrder SET reasonForMenu=? WHERE userIdx=? AND cartIdx=?";


        String InsertImage = "INSERT INTO ReviewImg (reviewIdx, reviewImgUrl) VALUES (?,?);";
        String DeleteImage = "UPDATE ReviewImg SET status='N' WHERE reviewIdx=?;";

        String isPhoto;
        if (imageList.size() == 0) {
            // 포토리뷰가 아닐 경우
            isPhoto = "N";
        } else {
            isPhoto = "Y";
        }


        this.jdbcTemplate.update(InsertReviewInfoQuery4, putReviewReq.getScore(), putReviewReq.getContent(),
                    isPhoto, putReviewReq.getReasonForStore(), putReviewReq.getIsDeliveryManGood(),
                putReviewReq.getReasonForDelivery(), reviewIdx);

        if (putReviewReq.getIsMenuGood().size()!=0){
            Set<Map.Entry<Integer,String>> entrySet = putReviewReq.getIsMenuGood().entrySet();
            Iterator<Map.Entry<Integer, String>> entryIterator = entrySet.iterator();

            while(entryIterator.hasNext()){
                Map.Entry<Integer, String> entry = entryIterator.next();
                Integer key = entry.getKey();
                String value = entry.getValue();
                this.jdbcTemplate.update(UpdateMenuIsGood, value, userIdx, key);
            }
        } else {
            Set<Map.Entry<Integer,String>> entrySet = putReviewReq.getReasonForMenu().entrySet();
            Iterator<Map.Entry<Integer, String>> entryIterator = entrySet.iterator();

            while(entryIterator.hasNext()){
                Map.Entry<Integer, String> entry = entryIterator.next();
                Integer key = entry.getKey();
                String value = entry.getValue();
                this.jdbcTemplate.update(UpdateMenuReason, value, userIdx, key);
            }
        }

        this.jdbcTemplate.update(DeleteImage, reviewIdx);
        for (String url:imageList){
            this.jdbcTemplate.update(InsertImage, reviewIdx, url);
        }
        return 1;

    }




    /**
     * 리뷰 삭제 API
     * [PATCH] /stores/review
     * @return BaseResponse<String>
     */
    public int deleteReview(int userOrderIdx) {
        String Query = "UPDATE Review SET status='N' WHERE userOrderIdx=?;";
        return this.jdbcTemplate.update(Query, userOrderIdx);
    }

    /**
     * 리뷰 도움이 돼요 등록 API
     * [POST] /stores/review/liked?reviewIdx
     * /liked?reviewIdx
     * @return BaseResponse<String>
     */
    public int createReviewLiked(int userIdx, int reviewIdx, String isHelped) {
        String Query = "INSERT INTO ReviewLiked (reviewIdx, userIdx, isHelped) VALUES(?,?,?);";
        return this.jdbcTemplate.update(Query, reviewIdx, userIdx, isHelped);
    }



    // 기존 좋아요 삭제
    public int deleteExistsReviewLiked(int userIdx, int reviewIdx) {
        String Query = "UPDATE ReviewLiked SET status='N' WHERE reviewIdx=? AND userIdx=? AND status='Y';";
        return this.jdbcTemplate.update(Query, reviewIdx, userIdx);
    }




    // 가게 존재 여부 확인
    public int checkStore(int storeIdx) {
        String Query = "SELECT EXISTS( SELECT * FROM Store WHERE status='Y' AND storeIdx=?);";
        int Param = storeIdx;

        return this.jdbcTemplate.queryForObject(Query,
                int.class,
                Param);
    }

    // 메뉴 존재 여부 확인
    public int checkMenu(int menuIdx) {
        String Query = "SELECT EXISTS( SELECT * FROM Menu WHERE status='Y' AND menuIdx=?);";
        int Param = menuIdx;

        return this.jdbcTemplate.queryForObject(Query,
                int.class,
                Param);
    }

    // 메뉴가 속한 가게 아이디 확인
    public int checkMenuOwner(int menuIdx) {
        String Query = "SELECT storeIdx FROM Menu WHERE menuIdx=?;";
        int Param = menuIdx;

        return this.jdbcTemplate.queryForObject(Query,
                int.class,
                Param);
    }



    // 사용자의 현재 위치 찾기
    public UserLocation getNowUserLocation(int userIdx) {
        String nowCheckQuery = "SELECT EXISTS(SELECT * FROM UserAddress WHERE userIdx=? AND isNowLocation='Y' AND status='Y');";

        String nowQuery = "SELECT addressLongitude, addressLatitude\n" +
                "                FROM UserAddress\n" +
                "                WHERE userIdx=? AND isNowLocation='Y' AND status='Y';";

        if (this.jdbcTemplate.queryForObject(nowCheckQuery, int.class, userIdx) != 0){
            return this.jdbcTemplate.queryForObject(nowQuery,
                    (rs, rowNum)-> new UserLocation(
                            rs.getDouble("addressLongitude"),
                            rs.getDouble("addressLatitude")
                    ), userIdx);
        }
        return new UserLocation(0.0,0.0);

    }

    // 홈화면 조회 -> 이츠에만 있는 맛집 가게 리스트
    public List<Integer> findOnlyEatsStoreIdxList() {
        String Query = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "WHERE S.status!='N' AND S.isOnlyEats='Y';";
        return this.jdbcTemplate.query(Query,
                (rs, rowNum) -> {
                    return rs.getInt("storeIdx");
                });

    }

    // 타입별 홈화면 조회 -> 이츠에만 있는 맛집 가게 리스트
    public List<Integer> findOnlyEatsStoreIdxList(UserLocation userLocation, GetStoreHomeReq getStoreHomeReq) {
        String OrderQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, COUNT(UO.userOrderIdx) AS orderCount\n" +
                "    FROM UserOrder UO\n" +
                "    GROUP BY UO.storeIdx) OC ON OC.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND S.isOnlyEats='Y' AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY OC.orderCount DESC;";

        String DistanceQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND S.isOnlyEats='Y' AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY ROUND(ST_DISTANCE_SPHERE(POINT(S.storeLongitude,S.storeLatitude), POINT(?,?))*0.001,1);";

        String ScoreQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    WHERE R.status='Y'\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND S.isOnlyEats='Y' AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY R.reviewScore DESC;";

        String NewQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "WHERE S.status!='N' AND S.isOnlyEats='Y' AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY S.createdAt DESC;";



        Object[] Params = new Object[]{getStoreHomeReq.getIsCheetah(), getStoreHomeReq.getMinimumPrice(), getStoreHomeReq.getIsToGo(),
                getStoreHomeReq.getIsCoupon(), getStoreHomeReq.getDeliveryFee()};
        Object[] DistanceParams = new Object[]{getStoreHomeReq.getIsCheetah(), getStoreHomeReq.getMinimumPrice(), getStoreHomeReq.getIsToGo(),
                getStoreHomeReq.getIsCoupon(), getStoreHomeReq.getDeliveryFee(), userLocation.getUserLongitude(), userLocation.getUserLatitude()};


        if (getStoreHomeReq.getSort().equals("distance")){
            return this.jdbcTemplate.query(DistanceQuery,
                    (rs, rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, DistanceParams);
        } else if (getStoreHomeReq.getSort().equals("score")){
            return this.jdbcTemplate.query(ScoreQuery,
                    (rs, rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, Params);
        } else if (getStoreHomeReq.getSort().equals("new")){
            return this.jdbcTemplate.query(NewQuery,
                    (rs, rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, Params);
        }
        return this.jdbcTemplate.query(OrderQuery,
                (rs, rowNum) -> {
                    return rs.getInt("storeIdx");
                }, Params);

    }

    // 홈화면 조회 -> 인기있는 프랜차이즈 가게 리스트
    public List<Integer> findFranchiseStoreIdxList() {
        String Query = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "    JOIN StoreCategoryMapping SCM on S.storeIdx = SCM.storeIdx\n" +
                "    JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
                "WHERE S.status!='N' AND SCM.storeCategoryIdx=23;";
        return this.jdbcTemplate.query(Query,
                (rs, rowNum) -> {
                    return rs.getInt("storeIdx");
                });
    }

    public List<Integer> findFranchiseStoreIdxList(UserLocation userLocation, GetStoreHomeReq getStoreHomeReq) {

        String CategoryOrderQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "    JOIN StoreCategoryMapping SCM on S.storeIdx = SCM.storeIdx\n" +
                "    JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, COUNT(UO.userOrderIdx) AS orderCount\n" +
                "    FROM UserOrder UO\n" +
                "    GROUP BY UO.storeIdx) OC ON OC.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND SCM.storeCategoryIdx=23 AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY OC.orderCount DESC;";

        String CategoryDistanceQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "    JOIN StoreCategoryMapping SCM on S.storeIdx = SCM.storeIdx\n" +
                "    JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND SCM.storeCategoryIdx=23 AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY ROUND(ST_DISTANCE_SPHERE(POINT(S.storeLongitude,S.storeLatitude), POINT(?,?))*0.001,1);";

        String CategoryScoreQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "    JOIN StoreCategoryMapping SCM on S.storeIdx = SCM.storeIdx\n" +
                "    JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    WHERE R.status='Y'\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND SCM.storeCategoryIdx=23 AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY R.reviewScore DESC;";

        String CategoryNewQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "    JOIN StoreCategoryMapping SCM on S.storeIdx = SCM.storeIdx\n" +
                "    JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "WHERE S.status!='N' AND SCM.storeCategoryIdx=23 AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY S.createdAt DESC;";



        Object[] Params = new Object[]{getStoreHomeReq.getIsCheetah(), getStoreHomeReq.getMinimumPrice(), getStoreHomeReq.getIsToGo(),
                getStoreHomeReq.getIsCoupon(), getStoreHomeReq.getDeliveryFee()};
        Object[] DistanceParams = new Object[]{getStoreHomeReq.getIsCheetah(), getStoreHomeReq.getMinimumPrice(), getStoreHomeReq.getIsToGo(),
                getStoreHomeReq.getIsCoupon(), getStoreHomeReq.getDeliveryFee(), userLocation.getUserLongitude(), userLocation.getUserLatitude()};


        if (getStoreHomeReq.getSort().equals("distance")){
            return this.jdbcTemplate.query(CategoryDistanceQuery,
                    (rs, rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, DistanceParams);
        } else if (getStoreHomeReq.getSort().equals("score")){
            return this.jdbcTemplate.query(CategoryScoreQuery,
                    (rs, rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, Params);
        } else if (getStoreHomeReq.getSort().equals("new")){
            return this.jdbcTemplate.query(CategoryNewQuery,
                    (rs, rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, Params);
        }
        return this.jdbcTemplate.query(CategoryOrderQuery,
                (rs, rowNum) -> {
                    return rs.getInt("storeIdx");
                }, Params);

    }


    // 홈화면 조회 -> 새로 들어왔어요 가게 리스트
    public List<Integer> findNewStoreIdxList(){
        String Query = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "WHERE S.status!='N' AND DATEDIFF(CURRENT_DATE(), S.createdAt)<=14;";
        return this.jdbcTemplate.query(Query,
                (rs, rowNum) -> {
                    return rs.getInt("storeIdx");
                });
    }

    public List<Integer> findNewStoreIdxList(UserLocation userLocation, GetStoreHomeReq getStoreHomeReq){
        String OrderQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, COUNT(UO.userOrderIdx) AS orderCount\n" +
                "    FROM UserOrder UO\n" +
                "    GROUP BY UO.storeIdx) OC ON OC.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND DATEDIFF(CURRENT_DATE(), S.createdAt)<=14 AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY OC.orderCount DESC;";

        String DistanceQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND DATEDIFF(CURRENT_DATE(), S.createdAt)<=14 AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY ROUND(ST_DISTANCE_SPHERE(POINT(S.storeLongitude,S.storeLatitude), POINT(?,?))*0.001,1);";

        String ScoreQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    WHERE R.status='Y'\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND DATEDIFF(CURRENT_DATE(), S.createdAt)<=14 AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY R.reviewScore DESC;";

        String NewQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "WHERE S.status!='N' AND DATEDIFF(CURRENT_DATE(), S.createdAt)<=14 AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY S.createdAt DESC;";



        Object[] Params = new Object[]{getStoreHomeReq.getIsCheetah(), getStoreHomeReq.getMinimumPrice(), getStoreHomeReq.getIsToGo(),
                getStoreHomeReq.getIsCoupon(), getStoreHomeReq.getDeliveryFee()};
        Object[] DistanceParams = new Object[]{getStoreHomeReq.getIsCheetah(), getStoreHomeReq.getMinimumPrice(), getStoreHomeReq.getIsToGo(),
                getStoreHomeReq.getIsCoupon(), getStoreHomeReq.getDeliveryFee(), userLocation.getUserLongitude(), userLocation.getUserLatitude()};


        if (getStoreHomeReq.getSort().equals("distance")){
            return this.jdbcTemplate.query(DistanceQuery,
                    (rs, rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, DistanceParams);
        } else if (getStoreHomeReq.getSort().equals("score")){
            return this.jdbcTemplate.query(ScoreQuery,
                    (rs, rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, Params);
        } else if (getStoreHomeReq.getSort().equals("new")){
            return this.jdbcTemplate.query(NewQuery,
                    (rs, rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, Params);
        }
        return this.jdbcTemplate.query(OrderQuery,
                (rs, rowNum) -> {
                    return rs.getInt("storeIdx");
                }, Params);
    }

    // 홈 화면 조회 -> 기본 홈 가게 리스트
    public List<Integer> findStoreIdxList(int categoryIdx, UserLocation userLocation, GetStoreHomeReq getStoreHomeReq) {

        String OrderQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, COUNT(UO.userOrderIdx) AS orderCount\n" +
                "    FROM UserOrder UO\n" +
                "    GROUP BY UO.storeIdx) OC ON OC.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY OC.orderCount DESC;";

        String DistanceQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY ROUND(ST_DISTANCE_SPHERE(POINT(S.storeLongitude,S.storeLatitude), POINT(?,?))*0.001,1);";

        String ScoreQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    WHERE R.status='Y'\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY R.reviewScore DESC;";

        String NewQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "WHERE S.status!='N' AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY S.createdAt DESC;";

        String CategoryOrderQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "    JOIN StoreCategoryMapping SCM on S.storeIdx = SCM.storeIdx\n" +
                "    JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, COUNT(UO.userOrderIdx) AS orderCount\n" +
                "    FROM UserOrder UO\n" +
                "    GROUP BY UO.storeIdx) OC ON OC.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND SCM.storeCategoryIdx=? AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY OC.orderCount DESC;";

        String CategoryDistanceQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "    JOIN StoreCategoryMapping SCM on S.storeIdx = SCM.storeIdx\n" +
                "    JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND SCM.storeCategoryIdx=? AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY ROUND(ST_DISTANCE_SPHERE(POINT(S.storeLongitude,S.storeLatitude), POINT(?,?))*0.001,1);";

        String CategoryScoreQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "    JOIN StoreCategoryMapping SCM on S.storeIdx = SCM.storeIdx\n" +
                "    JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    WHERE R.status='Y'\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N' AND SCM.storeCategoryIdx=? AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY R.reviewScore DESC;";

        String CategoryNewQuery = "SELECT S.storeIdx\n" +
                "FROM Store S\n" +
                "    JOIN StoreCategoryMapping SCM on S.storeIdx = SCM.storeIdx\n" +
                "    JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "WHERE S.status!='N' AND SCM.storeCategoryIdx=? AND (S.isCheetah='Y' OR S.isCheetah=?) AND S.minimumPrice<=? AND (S.isToGo='Y' OR S.isToGo=?) AND (S.isCoupon='Y' OR S.isCoupon=?) AND F.fee<=?\n" +
                "ORDER BY S.createdAt DESC;";

        Object[] Params = new Object[]{getStoreHomeReq.getIsCheetah(), getStoreHomeReq.getMinimumPrice(), getStoreHomeReq.getIsToGo(),
                getStoreHomeReq.getIsCoupon(), getStoreHomeReq.getDeliveryFee()};
        Object[] CategoryParams = new Object[]{categoryIdx, getStoreHomeReq.getIsCheetah(), getStoreHomeReq.getMinimumPrice(), getStoreHomeReq.getIsToGo(),
                getStoreHomeReq.getIsCoupon(), getStoreHomeReq.getDeliveryFee()};
        Object[] DistanceParams = new Object[]{getStoreHomeReq.getIsCheetah(), getStoreHomeReq.getMinimumPrice(), getStoreHomeReq.getIsToGo(),
                getStoreHomeReq.getIsCoupon(), getStoreHomeReq.getDeliveryFee(), userLocation.getUserLongitude(), userLocation.getUserLatitude()};
        Object[] DistanceCategoryParams = new Object[]{categoryIdx, getStoreHomeReq.getIsCheetah(), getStoreHomeReq.getMinimumPrice(), getStoreHomeReq.getIsToGo(),
                getStoreHomeReq.getIsCoupon(), getStoreHomeReq.getDeliveryFee(), userLocation.getUserLongitude(), userLocation.getUserLatitude()};

        if (categoryIdx==0){
            if (getStoreHomeReq.getSort().equals("distance")){
                return this.jdbcTemplate.query(DistanceQuery,
                        (rs, rowNum) -> {
                            return rs.getInt("storeIdx");
                        }, DistanceParams);
            } else if (getStoreHomeReq.getSort().equals("score")){
                return this.jdbcTemplate.query(ScoreQuery,
                        (rs, rowNum) -> {
                            return rs.getInt("storeIdx");
                        }, Params);
            } else if (getStoreHomeReq.getSort().equals("new")){
                return this.jdbcTemplate.query(NewQuery,
                        (rs, rowNum) -> {
                            return rs.getInt("storeIdx");
                        }, Params);
            }
            return this.jdbcTemplate.query(OrderQuery,
                    (rs, rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, Params);

        }

        if (getStoreHomeReq.getSort().equals("distance")){
            return this.jdbcTemplate.query(CategoryDistanceQuery,
                    (rs, rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, DistanceCategoryParams);
        } else if (getStoreHomeReq.getSort().equals("score")){
            return this.jdbcTemplate.query(CategoryScoreQuery,
                    (rs, rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, CategoryParams);
        } else if (getStoreHomeReq.getSort().equals("new")){
            return this.jdbcTemplate.query(CategoryNewQuery,
                    (rs, rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, CategoryParams);
        }
        return this.jdbcTemplate.query(CategoryOrderQuery,
                (rs, rowNum) -> {
                    return rs.getInt("storeIdx");
                }, CategoryParams);

    }

    // 이미 좋아요 한 가게가 있는지 확인
    public int checkFavoriteStore(int userIdx, int storeIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM Favorite WHERE userIdx=? AND storeIdx=? AND status='Y');";
        return this.jdbcTemplate.queryForObject(Query, int.class, userIdx, storeIdx);
    }


    // 즐겨찾기 한 가게 idx
    public List<Integer> getFavoriteStoreIdx(int userIdx, String sort) {

        String OrderCheck = "SELECT EXISTS(SELECT F.storeIdx\n" +
                "FROM Favorite F JOIN UserOrder UO on F.storeIdx = UO.storeIdx\n" +
                "WHERE F.userIdx=? AND F.status='Y' AND UO.status!='N' AND UO.status!='F' AND UO.status!='E'\n" +
                "GROUP BY F.storeIdx\n" +
                "ORDER BY COUNT(F.storeIdx) DESC);";
        int userOrderCheck = this.jdbcTemplate.queryForObject(OrderCheck, int.class, userIdx);


        String OrderCountQuery = "SELECT F.storeIdx\n" +
                "FROM Favorite F JOIN UserOrder UO on F.storeIdx = UO.storeIdx\n" +
                "WHERE F.userIdx=? AND F.status='Y' AND UO.status!='N' AND UO.status!='F' AND UO.status!='E'\n" +
                "GROUP BY F.storeIdx\n" +
                "ORDER BY COUNT(F.storeIdx) DESC;";

        String OrderTimeQuery = "SELECT F.storeIdx, OT.orderTime\n" +
                "FROM Favorite F JOIN(\n" +
                "    (SELECT RankRow.storeIdx, RankRow.orderTime\n" +
                "FROM (SELECT*, RANK() OVER (PARTITION BY UO.storeIdx ORDER BY UO.orderTime DESC) AS a\n" +
                "      FROM UserOrder UO\n" +
                "    WHERE UO.userIdx=? AND UO.status!='N' AND UO.status!='F' AND UO.status!='E'\n" +
                "     ) AS RankRow\n" +
                "WHERE RankRow.a <= 1)) OT ON OT.storeIdx = F.storeIdx\n" +
                "WHERE F.userIdx=? AND F.status='Y'\n" +
                "ORDER BY OT.orderTime DESC;";

        String AddTime = "SELECT storeIdx, DATE_FORMAT(createdAt, '%Y-%m-%d %H:%i:%s')\n" +
                "FROM Favorite\n" +
                "WHERE userIdx=? AND status='Y'\n" +
                "ORDER BY createdAt DESC;";


        if (userOrderCheck == 0 || sort.equals("recentAdd")){
            return this.jdbcTemplate.query(AddTime,
                    (rs,rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, userIdx);
        } else if (sort.equals("recentOrder")){
            return this.jdbcTemplate.query(OrderTimeQuery,
                    (rs,rowNum) -> {
                        return rs.getInt("storeIdx");
                    }, userIdx, userIdx);

        }

        return this.jdbcTemplate.query(OrderCountQuery,
                (rs,rowNum) -> {
                    return rs.getInt("storeIdx");
                }, userIdx);

    }


    // 가게별 리뷰 idx
    public List<StoreReviewIdx> getStoreReviewIdx(int storeIdx, String sort, String isPhoto) {
        String RecentPhotoQuery = "SELECT R.reviewIdx, R.userIdx, R.userOrderIdx\n" +
                "FROM Review R JOIN UserOrder UO on R.userOrderIdx = UO.userOrderIdx\n" +
                "WHERE UO.storeIdx=? AND R.status='Y' AND R.isPhoto='Y'\n" +
                "ORDER BY R.createdAt DESC;";

        String RecentQuery = "SELECT R.reviewIdx, R.userIdx, R.userOrderIdx\n" +
                "FROM Review R JOIN UserOrder UO on R.userOrderIdx = UO.userOrderIdx\n" +
                "WHERE UO.storeIdx=? AND R.status='Y'\n" +
                "ORDER BY R.createdAt DESC;";

        String ReviewPhotoQuery = "SELECT R.reviewIdx, R.userIdx, R.userOrderIdx\n" +
                "FROM Review R JOIN UserOrder UO on R.userOrderIdx = UO.userOrderIdx\n" +
                "LEFT JOIN (SELECT RL.reviewIdx, COUNT(RL.isHelped) AS isHelped\n" +
                "FROM ReviewLiked RL\n" +
                "WHERE RL.isHelped='G'\n" +
                "GROUP BY RL.reviewIdx\n" +
                "ORDER BY COUNT(RL.isHelped) DESC) Liked ON Liked.reviewIdx = R.reviewIdx\n" +
                "WHERE UO.storeIdx=? AND R.status='Y' AND R.isPhoto='Y'\n" +
                "ORDER BY isHelped DESC, R.createdAt DESC;";

        String ReviewQuery = "SELECT R.reviewIdx, R.userIdx, R.userOrderIdx\n" +
                "FROM Review R JOIN UserOrder UO on R.userOrderIdx = UO.userOrderIdx\n" +
                "LEFT JOIN (SELECT RL.reviewIdx, COUNT(RL.isHelped) AS isHelped\n" +
                "FROM ReviewLiked RL\n" +
                "WHERE RL.isHelped='G'\n" +
                "GROUP BY RL.reviewIdx\n" +
                "ORDER BY COUNT(RL.isHelped) DESC) Liked ON Liked.reviewIdx = R.reviewIdx\n" +
                "WHERE UO.storeIdx=? AND R.status='Y'\n" +
                "ORDER BY isHelped DESC, R.createdAt DESC;";

        String ScoreDescPhotoQuery = "SELECT R.reviewIdx, R.userIdx, R.userOrderIdx\n" +
                "FROM Review R JOIN UserOrder UO on R.userOrderIdx = UO.userOrderIdx\n" +
                "WHERE UO.storeIdx=? AND R.status='Y' AND R.isPhoto='Y'\n" +
                "ORDER BY R.score DESC;\n";

        String ScoreDescQuery = "SELECT R.reviewIdx, R.userIdx, R.userOrderIdx\n" +
                "FROM Review R JOIN UserOrder UO on R.userOrderIdx = UO.userOrderIdx\n" +
                "WHERE UO.storeIdx=? AND R.status='Y' AND R.isPhoto='Y'\n" +
                "ORDER BY R.score DESC;\n";

        String ScoreAscPhotoQuery = "SELECT R.reviewIdx, R.userIdx, R.userOrderIdx\n" +
                "FROM Review R JOIN UserOrder UO on R.userOrderIdx = UO.userOrderIdx\n" +
                "WHERE UO.storeIdx=? AND R.status='Y' AND R.isPhoto='Y'\n" +
                "ORDER BY R.score;";

        String ScoreAscQuery = "SELECT R.reviewIdx, R.userIdx, R.userOrderIdx\n" +
                "FROM Review R JOIN UserOrder UO on R.userOrderIdx = UO.userOrderIdx\n" +
                "WHERE UO.storeIdx=? AND R.status='Y'\n" +
                "ORDER BY R.score;";

        if (sort.equals("review")){
            if (isPhoto.equals("Y")){
                // 리뷰 도움 순 정렬 + 포토리뷰

                return this.jdbcTemplate.query(ReviewPhotoQuery,
                        (rs, rowNum) -> new StoreReviewIdx(
                                rs.getInt("reviewIdx"),
                                rs.getInt("userOrderIdx"),
                                rs.getInt("userIdx")),
                        storeIdx);
            }
            return this.jdbcTemplate.query(ReviewQuery,
                    (rs, rowNum) -> new StoreReviewIdx(
                            rs.getInt("reviewIdx"),
                            rs.getInt("userOrderIdx"),
                            rs.getInt("userIdx")),
                    storeIdx);

        } else if (sort.equals("highScore")){
            if (isPhoto.equals("Y")){
                // 별점 높은 순 정렬 + 포토리뷰
                return this.jdbcTemplate.query(ScoreDescPhotoQuery,
                        (rs, rowNum) -> new StoreReviewIdx(
                                rs.getInt("reviewIdx"),
                                rs.getInt("userOrderIdx"),
                                rs.getInt("userIdx")),
                        storeIdx);
            }
            return this.jdbcTemplate.query(ScoreDescQuery,
                    (rs, rowNum) -> new StoreReviewIdx(
                            rs.getInt("reviewIdx"),
                            rs.getInt("userOrderIdx"),
                            rs.getInt("userIdx")),
                    storeIdx);

        } else if (sort.equals("rowScore")) {

            if (isPhoto.equals("Y")){
                // 별점 낮은 순 정렬 + 포토리뷰
                return this.jdbcTemplate.query(ScoreAscPhotoQuery,
                        (rs, rowNum) -> new StoreReviewIdx(
                                rs.getInt("reviewIdx"),
                                rs.getInt("userOrderIdx"),
                                rs.getInt("userIdx")),
                        storeIdx);
            }
            return this.jdbcTemplate.query(ScoreAscQuery,
                    (rs, rowNum) -> new StoreReviewIdx(
                            rs.getInt("reviewIdx"),
                            rs.getInt("userOrderIdx"),
                            rs.getInt("userIdx")),
                    storeIdx);
        }

        if (isPhoto.equals("Y")){
            // 최근 순 정렬 + 포토리뷰
            return this.jdbcTemplate.query(RecentPhotoQuery,
                    (rs, rowNum) -> new StoreReviewIdx(
                            rs.getInt("reviewIdx"),
                            rs.getInt("userOrderIdx"),
                            rs.getInt("userIdx")),
                    storeIdx);
        }

        return this.jdbcTemplate.query(RecentQuery,
                (rs, rowNum) -> new StoreReviewIdx(
                        rs.getInt("reviewIdx"),
                        rs.getInt("userOrderIdx"),
                        rs.getInt("userIdx")),
                storeIdx);
    }
    // 리뷰 존재 확인 - userIdx, userOrderIdx
    public int checkUserReview(int userIdx, int userOrderIdx) {
        String Query = "SELECT EXISTS(SELECT reviewIdx\n" +
                "FROM UserOrder UO JOIN Review R on UO.userOrderIdx = R.userOrderIdx\n" +
                "WHERE UO.userOrderIdx=? AND UO.userIdx=? AND R.status='Y');";
        return this.jdbcTemplate.queryForObject(Query, int.class, userOrderIdx, userIdx);
    }

    // 리뷰 작성자 확인
    public int checkReviewOwner(int userIdx, int reviewIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM Review WHERE userIdx=? AND reviewIdx=? AND status='Y');";
        return this.jdbcTemplate.queryForObject(Query, int.class, userIdx, reviewIdx);
    }

    // 리뷰 수정 기간 확인
    public boolean checkReviewUploadTime(int reviewIdx) {
        String Query ="SELECT TIMESTAMPDIFF(DAY, R.createdAt, CURRENT_TIMESTAMP())\n" +
                "FROM Review R\n" +
                "WHERE reviewIdx=?;";
        int date = this.jdbcTemplate.queryForObject(Query, int.class, reviewIdx);
        if (date >=10){
            return false;
        } else{
            return true;
        }
    }

    // 리뷰 작성 기한 확인
    public boolean checkOrderTime(int userOrderIdx) {
        String Query = "SELECT TIMESTAMPDIFF(DAY, UO.orderTime, CURRENT_TIMESTAMP())\n" +
                "FROM UserOrder UO\n" +
                "WHERE userOrderIdx=?;";

        int date = this.jdbcTemplate.queryForObject(Query, int.class, userOrderIdx);
        if (date >=10){
            return false;
        } else{
            return true;
        }
    }

    // 리뷰 아이디 찾기
    public int findReviewIdx(int userOrderIdx) {
        String Query1 = "SELECT EXISTS(SELECT reviewIdx FROM Review WHERE userOrderIdx=? AND status='Y');";

        String Query2 = "SELECT reviewIdx FROM Review WHERE userOrderIdx=? AND status='Y';";

        if (this.jdbcTemplate.queryForObject(Query1, int.class, userOrderIdx)==0){
            return 0;
        }
        return this.jdbcTemplate.queryForObject(Query2, int.class, userOrderIdx);
    }


    // 이미 리뷰한 글인지 확인
    public String checkLikedReview(int userIdx, int reviewIdx) {
        String CheckIsExists = "SELECT EXISTS(SELECT isHelped FROM ReviewLiked WHERE reviewIdx=? AND userIdx=? AND status='Y');";
        if (this.jdbcTemplate.queryForObject(CheckIsExists, int.class, reviewIdx, userIdx)==0){
            return "N";
        }
        String FindType = "SELECT isHelped FROM ReviewLiked WHERE reviewIdx=? AND userIdx=? AND status='Y';";
        return this.jdbcTemplate.queryForObject(FindType, String.class, reviewIdx, userIdx);
    }

    // 리뷰가 존재하는지 확인 - reviewIdx
    public int checkReviewExists(int reviewIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM Review WHERE reviewIdx=? AND status='Y');";
        return this.jdbcTemplate.queryForObject(Query, int.class, reviewIdx);
    }

    // 가게 검색 조회 -> idx 찾기
    public List<Integer> findKeywordStoreIdxList(String keyword) {

        String Query = "SELECT DISTINCT S.storeIdx\n" +
                "FROM Store S\n" +
                "    JOIN StoreCategoryMapping SCM on S.storeIdx = SCM.storeIdx\n" +
                "    JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
                "    JOIN Menu M on S.storeIdx = M.storeIdx\n" +
                "WHERE M.status!='N' AND M.status != 'H' AND S.status!='N'\n" +
                "  AND (INSTR(categoryName , ?) > 0 OR INSTR(S.storeName , ?) > 0) OR INSTR(M.menuName , ?) > 0;";

        return this.jdbcTemplate.query(Query,
                (rs, rowNum) -> {
                    return rs.getInt("storeIdx");
                }, keyword, keyword, keyword);

    }

}
