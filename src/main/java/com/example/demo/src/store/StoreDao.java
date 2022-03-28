package com.example.demo.src.store;

import com.example.demo.src.store.model.*;
import com.example.demo.src.store.model.Req.PostReviewReq;
import com.example.demo.src.store.model.Res.*;
import com.example.demo.src.user.model.UserLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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
     * 홈 화면 조회 API
     * [GET] /stores/home
     * @return BaseResponse<List<GetStoreHomeRes>>
     */
    public GetStoreHomeRes getStoreHome(int idx, UserLocation userLocation) {

//        // 주문 많은 순
//        String Query1 = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount, F.fee, S.isToGo, S.storeLongitude, S.storeLatitude, S.status\n" +
//                "FROM Store S\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
//                "    FROM DeliveryFee\n" +
//                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
//                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
//                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT UO.storeIdx, COUNT(UO.userOrderIdx) AS orderCount\n" +
//                "    FROM UserOrder UO\n" +
//                "    GROUP BY UO.storeIdx) OC ON OC.storeIdx=S.storeIdx\n" +
//                "WHERE S.status != 'N' AND  (S.isCheetah=? OR S.isCheetah=?) AND (S.isToGo=? OR S.isToGo=?) AND (S.isCoupon=? OR S.isCoupon=?) AND F.fee <= ? AND S.minimumPrice <=?\n" +
//                "ORDER BY OC.orderCount DESC;";
//
//        // 별점 높은 순
//        String Query2 = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount, F.fee, S.isToGo, S.storeLongitude, S.storeLatitude, S.status\n" +
//                "FROM Store S\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
//                "    FROM DeliveryFee\n" +
//                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
//                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
//                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
//                "WHERE S.status != 'N' AND  (S.isCheetah=? OR S.isCheetah=?) AND (S.isToGo=? OR S.isToGo=?) AND (S.isCoupon=? OR S.isCoupon=?) AND F.fee <= ? AND S.minimumPrice <=?\n" +
//                "ORDER BY R.reviewScore DESC;";
//
//        // 신규 매장 순
//        String Query3 = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount, F.fee, S.isToGo, S.storeLongitude, S.storeLatitude, S.status\n" +
//                "FROM Store S\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
//                "    FROM DeliveryFee\n" +
//                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
//                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
//                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
//                "WHERE S.status != 'N' AND  (S.isCheetah=? OR S.isCheetah=?) AND (S.isToGo=? OR S.isToGo=?) AND (S.isCoupon=? OR S.isCoupon=?) AND F.fee <= ? AND S.minimumPrice <=?\n" +
//                "ORDER BY S.createdAt DESC;";
//
//        // 주문 많은 순 + 카테고리
//        String Query1_Category = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount, F.fee, S.isToGo, S.isCoupon, S.storeLongitude, S.storeLatitude, S.status\n" +
//                "FROM Store S\n" +
//                "JOIN (\n" +
//                "    SELECT SCM.storeIdx, SCM.storeCategoryIdx, SC.categoryName\n" +
//                "    FROM StoreCategoryMapping SCM JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
//                "    WHERE SCM.storeCategoryIdx=?) Category ON Category.storeIdx = S.storeIdx\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
//                "    FROM DeliveryFee\n" +
//                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
//                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
//                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT UO.storeIdx, COUNT(UO.userOrderIdx) AS orderCount\n" +
//                "    FROM UserOrder UO\n" +
//                "    GROUP BY UO.storeIdx) OC ON OC.storeIdx=S.storeIdx\n" +
//                "WHERE S.status != 'N' AND  (S.isCheetah=? OR S.isCheetah=?) AND (S.isToGo=? OR S.isToGo=?) AND (S.isCoupon=? OR S.isCoupon=?) AND F.fee <= ? AND S.minimumPrice <=?\n" +
//                "ORDER BY OC.orderCount DESC;";
//
//        // 별점 높은 순 + 카테고리
//        String Query2_Category = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount, F.fee, S.isToGo, S.isCoupon, S.storeLongitude, S.storeLatitude, S.status\n" +
//                "FROM Store S\n" +
//                "JOIN (\n" +
//                "    SELECT SCM.storeIdx, SCM.storeCategoryIdx, SC.categoryName\n" +
//                "    FROM StoreCategoryMapping SCM JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
//                "    WHERE SCM.storeCategoryIdx=?) Category ON Category.storeIdx = S.storeIdx\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
//                "    FROM DeliveryFee\n" +
//                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
//                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
//                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
//                "WHERE S.status != 'N' AND  (S.isCheetah=? OR S.isCheetah=?) AND (S.isToGo=? OR S.isToGo=?) AND (S.isCoupon=? OR S.isCoupon=?) AND F.fee <= ? AND S.minimumPrice <=?\n" +
//                "ORDER BY R.reviewScore DESC;";
//
//        // 신규 매장 순 + 카테고리
//        String Query3_Category = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount, F.fee, S.isToGo, S.isCoupon, S.storeLongitude, S.storeLatitude, S.status\n" +
//                "FROM Store S\n" +
//                "JOIN (\n" +
//                "    SELECT SCM.storeIdx, SCM.storeCategoryIdx, SC.categoryName\n" +
//                "    FROM StoreCategoryMapping SCM JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
//                "    WHERE SCM.storeCategoryIdx=?) Category ON Category.storeIdx = S.storeIdx\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
//                "    FROM DeliveryFee\n" +
//                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
//                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
//                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
//                "WHERE S.status != 'N' AND  (S.isCheetah=? OR S.isCheetah=?) AND (S.isToGo=? OR S.isToGo=?) AND (S.isCoupon=? OR S.isCoupon=?) AND F.fee <= ? AND S.minimumPrice <=?\n" +
//                "ORDER BY S.createdAt DESC;";
//
//
//        String StoreCouponQuery = "SELECT S.storeIdx, IFNULL(C.discountPrice,0) AS maxDiscountPrice, IFNULL(C.couponType,'N') AS couponType\n" +
//                "FROM Store S\n" +
//                "LEFT JOIN (SELECT RankRow.storeIdx, RankRow.discountPrice, RankRow.couponType\n" +
//                "            FROM (SELECT*, RANK() OVER (PARTITION BY storeIdX ORDER BY discountPrice DESC, couponIdx ASC) AS a\n" +
//                "                  FROM Coupon\n" +
//                "                WHERE status='Y' AND  DATEDIFF(endDate, CURRENT_DATE())>=0\n" +
//                "                 ) AS RankRow\n" +
//                "            WHERE RankRow.a <= 1) C ON C.storeIdx = S.storeIdx\n" +
//                "WHERE S.storeIdx = ?;";
//
//        String StoreMenuImgQuery = "SELECT RankRow.storeIdx, RankRow.menuImgUrl\n" +
//                "FROM (SELECT*, RANK() OVER (PARTITION BY M.storeIdX ORDER BY M.menuIdx) AS a\n" +
//                "      FROM Menu M\n" +
//                "     ) AS RankRow\n" +
//                "WHERE RankRow.a <= 2 AND RankRow.storeIdx=?;";
//
//
//
//
//        if (storeHome.getSort()==null){storeHome.setSort("default");}
//        if (storeHome.getIsCheetah()==null){storeHome.setIsCheetah("N");}
//        if (storeHome.getFee()==0){storeHome.setFee(5000);}
//        if (storeHome.getMinimum()==0){storeHome.setMinimum(100000);}
//        if (storeHome.getIsToGo()==null){storeHome.setIsToGo("N");}
//        if (storeHome.getIsCoupon()==null){storeHome.setIsCoupon("N");}
//
//        Object[] Params = new Object[]{storeHome.getIsCheetah(),"Y", storeHome.getIsToGo(),"Y", storeHome.getIsCoupon(),"Y", storeHome.getFee(), storeHome.getMinimum()};
//        Object[] Params_Category = new Object[]{storeHome.getCategoryIdx(), storeHome.getIsCheetah(),"Y", storeHome.getIsToGo(),"Y", storeHome.getIsCoupon(),"Y", storeHome.getFee(), storeHome.getMinimum()};
//
//
//        // 카테고리가 있을 경우
//        if (storeHome.getCategoryIdx()!=0){
//            if (storeHome.getSort().equals("score")){
//                return this.jdbcTemplate.query(Query2_Category,
//                        (rs1, rowNum1) -> new GetStoreHomeRes(
//                                rs1.getString("storeImgUrl"),
//                                rs1.getString("storeName"),
//                                rs1.getString("isCheetah"),
//                                rs1.getString("timeDelivery"),
//                                rs1.getDouble("reviewScore"),
//                                rs1.getInt("reviewCount"),
//                                rs1.getInt("fee"),
//                                rs1.getString("isToGo"),
//                                rs1.getDouble("storeLongitude"),
//                                rs1.getDouble("storeLatitude"),
//                                rs1.getString("status"),
//                                this.jdbcTemplate.queryForObject(StoreCouponQuery,
//                                        (rs2, rowNum2) -> new StoreCouponInfo(
//                                                rs2.getInt("maxDiscountPrice"),
//                                                rs2.getString("couponType")
//                                        ), rs1.getInt("storeIdx")),
//                                this.jdbcTemplate.query(StoreMenuImgQuery,
//                                        (rs3, rowNum3) -> new String(
//                                                rs3.getString("menuImgUrl")
//                                        ), rs1.getInt("storeIdx"))
//
//                        ), Params_Category);
//
//            } else if (storeHome.getSort().equals("new")){
//                return this.jdbcTemplate.query(Query3_Category,
//                        (rs1, rowNum1) -> new GetStoreHomeRes(
//                                rs1.getString("storeImgUrl"),
//                                rs1.getString("storeName"),
//                                rs1.getString("isCheetah"),
//                                rs1.getString("timeDelivery"),
//                                rs1.getDouble("reviewScore"),
//                                rs1.getInt("reviewCount"),
//                                rs1.getInt("fee"),
//                                rs1.getString("isToGo"),
//                                rs1.getDouble("storeLongitude"),
//                                rs1.getDouble("storeLatitude"),
//                                rs1.getString("status"),
//                                this.jdbcTemplate.queryForObject(StoreCouponQuery,
//                                        (rs2, rowNum2) -> new StoreCouponInfo(
//                                                rs2.getInt("maxDiscountPrice"),
//                                                rs2.getString("couponType")
//                                        ), rs1.getInt("storeIdx")),
//                                this.jdbcTemplate.query(StoreMenuImgQuery,
//                                        (rs2, rowNum2) -> new String(
//                                                rs2.getString("menuImgUrl")
//                                        ), rs1.getInt("storeIdx"))
//
//                        ), Params_Category);
//
//            }
//            System.out.println("default");
//            return this.jdbcTemplate.query(Query1_Category,
//                    (rs1, rowNum1) -> new GetStoreHomeRes(
//                            rs1.getString("storeImgUrl"),
//                            rs1.getString("storeName"),
//                            rs1.getString("isCheetah"),
//                            rs1.getString("timeDelivery"),
//                            rs1.getDouble("reviewScore"),
//                            rs1.getInt("reviewCount"),
//                            rs1.getInt("fee"),
//                            rs1.getString("isToGo"),
//                            rs1.getDouble("storeLongitude"),
//                            rs1.getDouble("storeLatitude"),
//                            rs1.getString("status"),
//                            this.jdbcTemplate.queryForObject(StoreCouponQuery,
//                                    (rs2, rowNum2) -> new StoreCouponInfo(
//                                            rs2.getInt("maxDiscountPrice"),
//                                            rs2.getString("couponType")
//                                    ), rs1.getInt("storeIdx")),
//                            this.jdbcTemplate.query(StoreMenuImgQuery,
//                                    (rs2, rowNum2) -> new String(
//                                            rs2.getString("menuImgUrl")
//                                    ), rs1.getInt("storeIdx"))
//
//                    ), Params_Category);
//        }
//
//        if (storeHome.getSort().equals("score")){
//            return this.jdbcTemplate.query(Query2,
//                (rs1, rowNum1) -> new GetStoreHomeRes(
//                        rs1.getString("storeImgUrl"),
//                        rs1.getString("storeName"),
//                        rs1.getString("isCheetah"),
//                        rs1.getString("timeDelivery"),
//                        rs1.getDouble("reviewScore"),
//                        rs1.getInt("reviewCount"),
//                        rs1.getInt("fee"),
//                        rs1.getString("isToGo"),
//                        rs1.getDouble("storeLongitude"),
//                        rs1.getDouble("storeLatitude"),
//                        rs1.getString("status"),
//                        this.jdbcTemplate.queryForObject(StoreCouponQuery,
//                                (rs2, rowNum2) -> new StoreCouponInfo(
//                                        rs2.getInt("maxDiscountPrice"),
//                                        rs2.getString("couponType")
//                                ), rs1.getInt("storeIdx")),
//                        this.jdbcTemplate.query(StoreMenuImgQuery,
//                                (rs3, rowNum3) -> new String(
//                                        rs3.getString("menuImgUrl")
//                                ), rs1.getInt("storeIdx"))
//
//                ), Params);
//
//        } else if (storeHome.getSort().equals("new")){
//            return this.jdbcTemplate.query(Query3,
//                    (rs1, rowNum1) -> new GetStoreHomeRes(
//                            rs1.getString("storeImgUrl"),
//                            rs1.getString("storeName"),
//                            rs1.getString("isCheetah"),
//                            rs1.getString("timeDelivery"),
//                            rs1.getDouble("reviewScore"),
//                            rs1.getInt("reviewCount"),
//                            rs1.getInt("fee"),
//                            rs1.getString("isToGo"),
//                            rs1.getDouble("storeLongitude"),
//                            rs1.getDouble("storeLatitude"),
//                            rs1.getString("status"),
//                            this.jdbcTemplate.queryForObject(StoreCouponQuery,
//                                    (rs2, rowNum2) -> new StoreCouponInfo(
//                                            rs2.getInt("maxDiscountPrice"),
//                                            rs2.getString("couponType")
//                                    ), rs1.getInt("storeIdx")),
//                            this.jdbcTemplate.query(StoreMenuImgQuery,
//                                    (rs2, rowNum2) -> new String(
//                                            rs2.getString("menuImgUrl")
//                                    ), rs1.getInt("storeIdx"))
//
//                    ), Params);
//
//        }
//
//        return this.jdbcTemplate.query(Query1,
//                (rs1, rowNum1) -> new GetStoreHomeRes(
//                        rs1.getString("storeImgUrl"),
//                        rs1.getString("storeName"),
//                        rs1.getString("isCheetah"),
//                        rs1.getString("timeDelivery"),
//                        rs1.getDouble("reviewScore"),
//                        rs1.getInt("reviewCount"),
//                        rs1.getInt("fee"),
//                        rs1.getString("isToGo"),
//                        rs1.getDouble("storeLongitude"),
//                        rs1.getDouble("storeLatitude"),
//                        rs1.getString("status"),
//                        this.jdbcTemplate.queryForObject(StoreCouponQuery,
//                                (rs2, rowNum2) -> new StoreCouponInfo(
//                                        rs2.getInt("maxDiscountPrice"),
//                                        rs2.getString("couponType")
//                                ), rs1.getInt("storeIdx")),
//                        this.jdbcTemplate.query(StoreMenuImgQuery,
//                                (rs2, rowNum2) -> new String(
//                                        rs2.getString("menuImgUrl")
//                                ), rs1.getInt("storeIdx"))
//
//                ), Params);
//
//        String StoreInfoQuery = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount,\n" +
//                "       S.isToGo, S.isCoupon, S.storeLongitude, S.storeLatitude, S.status, S.minimumPrice, S.buildingName, S.storeAddress, S.storeAddressDetail, S.createdAt\n" +
//                "FROM Store S\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
//                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
//                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
//                "WHERE S.status != 'N';";
//
//        String DeliveryTimeQuery = "SELECT storeIdx, minPrice, maxPrice, deliveryFee FROM DeliveryFee WHERE storeIdx=?;";
//
//        String StoreCategoryQuery = "SELECT SCM.storeIdx, SC.storeCategoryIdx, categoryName\n" +
//                "FROM StoreCategoryMapping SCM JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
//                "WHERE SCM.storeIdx=?;";
//
//        String StoreCouponQuery = "SELECT couponIdx, storeIdx, couponTitle, discountPrice, limitPrice, endDate, couponType, createdAt, status FROM Coupon WHERE storeIdx=? AND status='Y';";
//
//        String orderCountQuery = "SELECT UO.storeIdx, COUNT(UO.userOrderIdx) AS orderCount\n" +
//                "FROM UserOrder UO\n" +
//                "WHERE storeIdx=?\n" +
//                "GROUP BY UO.storeIdx";
//
//        String StoreMenuImgQuery = "SELECT RankRow.storeIdx, RankRow.menuImgUrl\n" +
//                "FROM (SELECT*, RANK() OVER (PARTITION BY M.storeIdX ORDER BY M.menuIdx) AS a\n" +
//                "      FROM Menu M\n" +
//                "     ) AS RankRow\n" +
//                "WHERE RankRow.a <= 2 AND RankRow.storeIdx=?;";

        String StoreInfoQuery = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount,\n" +
                "                       S.isToGo, S.isCoupon, S.status, S.minimumPrice, S.buildingName, S.storeAddress, S.storeAddressDetail, DATE_FORMAT(S.createdAt, '%Y-%m-%d %H:%I:%S') AS createdAt,\n" +
                "       ROUND(ST_DISTANCE_SPHERE(POINT(S.storeLongitude,S.storeLatitude), POINT(?,?))*0.001,1) AS distance\n" +
                "                FROM Store S\n" +
                "                LEFT JOIN (\n" +
                "                    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "                    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx WHERE R.status='Y'\n" +
                "                    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "                WHERE S.status != 'N' AND S.storeIdx=?;";

//        String DeliveryFeeQuery = "SELECT IFNULL(MIN(deliveryFee),0) AS fee\n" +
//                "FROM DeliveryFee D\n" +
//                "WHERE D.storeIdx=? AND D.status='Y';";

        String DeliveryFeeCount = "SELECT COUNT(*) as feeCount\n" +
                "FROM DeliveryFee D\n" +
                "WHERE D.storeIdx=? AND D.status='Y';";

        String DeliveryFeeQuery = "SELECT CASE\n" +
                "    WHEN IFNULL(MIN(deliveryFee),0) =0\n" +
                "        THEN '무료배달'\n" +
                "    ELSE CONCAT(FORMAT(IFNULL(MIN(deliveryFee),0),0),'원')\n" +
                "    END AS fee\n" +
                "        FROM DeliveryFee D\n" +
                "        WHERE D.storeIdx=? AND D.status='Y';";


//        String StoreCategoryQuery = "SELECT SCM.storeIdx, SC.storeCategoryIdx, categoryName\n" +
//                "FROM StoreCategoryMapping SCM JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
//                "WHERE SCM.storeIdx=?;";

//        String StoreCouponQuery = "SELECT couponIdx, storeIdx, couponTitle, discountPrice, limitPrice, endDate, couponType, DATE_FORMAT(createdAt, '%Y-%m-%d %H:%I:%S') AS createdAt, status FROM Coupon WHERE storeIdx=? AND status='Y';";

        String orderCountQuery = "SELECT COUNT(UO.userOrderIdx) AS orderCount\n" +
                "FROM UserOrder UO\n" +
                "WHERE storeIdx=?\n" +
                "GROUP BY UO.storeIdx;";

        String StoreMenuImgQuery = "SELECT RankRow.menuImgUrl\n" +
                "FROM (SELECT*, RANK() OVER (PARTITION BY M.storeIdX ORDER BY M.menuIdx) AS a\n" +
                "      FROM Menu M\n" +
                "     ) AS RankRow\n" +
                "WHERE RankRow.a <= 2 AND RankRow.storeIdx=?;";

        String StoreCouponQuery = "SELECT C.couponIdx, CONCAT(FORMAT(IFNULL(C.discountPrice,0),0),'원') AS maxDiscountPrice, IFNULL(C.couponType,'N') AS couponType\n" +
                "FROM Store S\n" +
                "LEFT JOIN (SELECT RankRow.storeIdx, RankRow.couponIdx, RankRow.discountPrice, RankRow.couponType\n" +
                "            FROM (SELECT*, RANK() OVER (PARTITION BY storeIdX ORDER BY discountPrice DESC, couponIdx ASC) AS a\n" +
                "                    FROM Coupon\n" +
                "                    WHERE status='Y' AND  DATEDIFF(endDate, CURRENT_DATE())>=0) AS RankRow\n" +
                "                    WHERE RankRow.a <= 1) C ON C.storeIdx = S.storeIdx\n" +
                "WHERE S.storeIdx = ?;";

        Object[] Params = new Object[]{userLocation.getUserLongitude(), userLocation.getUserLatitude(), idx};
        System.out.println("here");

        int oc = 0;
        if (this.jdbcTemplate.queryForObject("SELECT EXISTS(SELECT * FROM UserOrder UO WHERE storeIdx=?);", int.class, idx)!=0){
            oc = this.jdbcTemplate.queryForObject(orderCountQuery,
                    int.class,
                    idx);
        }
        int orderCount = oc;

        int deliveryFeeCount =  this.jdbcTemplate.queryForObject(DeliveryFeeCount,
                int.class,
                idx);
        System.out.println("deliveryFeeCount" + deliveryFeeCount);
        String fee = this.jdbcTemplate.queryForObject(DeliveryFeeQuery,
                String.class,
                idx);

        if (deliveryFeeCount>1){
            fee = fee+"~";
        }
        System.out.println("fee" + fee);

        String deliveryFee = fee;
        StoreInfo storeInfo = this.jdbcTemplate.queryForObject(StoreInfoQuery,
                (rs1, rowNum1) -> new StoreInfo(
                        rs1.getInt("storeIdx"),
                        rs1.getString("storeImgUrl"),
                        rs1.getString("storeName"),
                        rs1.getString("isCheetah"),
                        rs1.getString("timeDelivery"),
                        rs1.getString("isToGo"),
                        rs1.getString("isCoupon"),
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
                        deliveryFee
                ), Params);
        System.out.println("storeInfo");

        StoreBestCoupon storeBestCoupon = this.jdbcTemplate.queryForObject(StoreCouponQuery,
                (rs2, rowNum2) -> new StoreBestCoupon(
                        rs2.getInt("couponIdx"),
                        rs2.getString("maxDiscountPrice"),
                        rs2.getString("couponType")
                ), idx);
        System.out.println("bestCoupon");
        List<String> storeMenuImg = this.jdbcTemplate.query(StoreMenuImgQuery,
                (rs1, rowNum1) -> new String(
                        rs1.getString("menuImgUrl")
                ),idx);
        System.out.println("storeMenuImg");


        return new GetStoreHomeRes(storeInfo, storeBestCoupon ,storeMenuImg);
    }

    /**
     * 가게 상세 화면 조회 API
     * [GET] /stores/detail?storeIdx=
     * @return BaseResponse<List<GetStoreHomeRes>>
     */
    public GetStoreDetailRes getStoreDetail(UserLocation userLocation, int storeIdx, int userIdx) {

        String StoreInfoQuery = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount,\n" +
                "       CASE WHEN S.isToGo='Y' THEN S.timeToGo ELSE 'N' END AS timeToGo,\n" +
                "       S.isToGo, S.isCoupon, S.status, S.minimumPrice, S.buildingName, S.storeAddress, S.storeAddressDetail,\n" +
                "       ROUND(ST_DISTANCE_SPHERE(POINT(S.storeLongitude,S.storeLatitude), POINT(?,?))*0.001,1) AS distance\n" +
                "                FROM Store S\n" +
                "                LEFT JOIN (\n" +
                "                    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "                    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx WHERE R.status='Y'\n" +
                "                    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "                WHERE S.status != 'N' AND S.storeIdx=?;";

        String StoreCouponQuery = "SELECT couponIdx, couponTitle, CONCAT(FORMAT(discountPrice,0),'원') AS discountPrice, \n" +
                "       CONCAT(FORMAT(limitPrice,0),'원 이상 주문 시') AS limitPrice,\n" +
                "       CONCAT(DATE_FORMAT(endDate, '%m/%d'),' 까지') AS endDate, couponType\n" +
                "FROM Coupon WHERE status='Y' AND storeIdx=? AND DATEDIFF(endDate, CURRENT_DATE())>=0;";

        String MinimumDeliveryFeeQuery = "SELECT IFNULL(MIN(deliveryFee),0) as fee\n" +
                "        FROM DeliveryFee D\n" +
                "        WHERE D.storeIdx=? AND D.status='Y';";

        String DeliveryFeeInfo = "SELECT storeIdx,\n" +
                "       CASE WHEN deliveryFee=0 THEN '무료' ELSE CONCAT(FORMAT(deliveryFee,0),'원') END AS deliveryFee,\n" +
                "       CASE WHEN maxPrice IS NULL THEN CONCAT(FORMAT(minPrice,0),'원 ~ ') ELSE CONCAT(FORMAT(minPrice,0), '원 ~ ', FORMAT(maxPrice,0),'원') END AS orderPrice\n" +
                "FROM DeliveryFee WHERE storeIdx=?;";

        String MenuCategoryQuery = "SELECT MC.menuCategoryIdx, storeIdx, categoryName\n" +
                "FROM MenuCategory MC\n" +
                "WHERE MC.storeIdx=? AND MC.status='Y';";

        String MenuDetailQuery = "SELECT menuIdx, menuName, menuPrice, menuDetail, menuImgUrl, isOption, status\n" +
                "FROM Menu\n" +
                "WHERE status!='N' AND menuCategoryIdx=?;";

        String MenuImageQuery = "SELECT RankRow.menuImgUrl\n" +
                "FROM (SELECT*, RANK() OVER (PARTITION BY M.menuIdx ORDER BY M.menuImgIdx) AS a\n" +
                "      FROM MenuImage M\n" +
                "     ) AS RankRow\n" +
                "WHERE RankRow.a <= 1 AND RankRow.menuIdx=?;";


        String checkPhotoReviewThree = "SELECT EXISTS(SELECT S.storeIdx, PR.photoReviewCount\n" +
                "FROM Store S JOIN (\n" +
                "    SELECT UO.storeIdx, COUNT(storeIdx) AS photoReviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx = UO.userOrderIdx\n" +
                "    WHERE R.isPhoto='Y' AND R.status='Y' AND UO.status!='N'\n" +
                "    GROUP BY storeIdx) PR ON PR.storeIdx = S.storeIdx\n" +
                "WHERE S.status='Y' AND PR.photoReviewCount>=3 AND S.storeIdx=?);";

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

        String IsManyOrderQuery = "SELECT CASE WHEN COUNT(*)>=? THEN 'Y' ELSE 'N' END AS isManyOrder\n" +
                "FROM CartToOrder CTO JOIN Cart C on CTO.cartIdx = C.cartIdx\n" +
                "WHERE C.menuIdx=? AND CTO.status!='N';";
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
                                (rs4, rowNum4) -> new MenuDetail(
                                        rs4.getInt("menuIdx"),
                                        rs4.getString("menuName"),
                                        rs4.getInt("menuPrice"),
                                        rs4.getString("menuDetail"),
                                        rs4.getString("menuImgUrl"),
                                        rs4.getString("isOption"),
                                        rs4.getString("status"),
                                        this.jdbcTemplate.queryForObject(IsManyOrderQuery,
                                                String.class,
                                                storeTotalOrder, rs4.getInt("menuIdx")),
                                        this.jdbcTemplate.queryForObject(IsManyReviewQuery,
                                                String.class,
                                                storeTotalMenuGood,rs4.getInt("menuIdx"))

                                ), rs3.getInt("menuCategoryIdx"))
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
        return this.jdbcTemplate.queryForObject(StoreInfoQuery,
                (rs, rowNum) -> new GetStoreDetailRes(
                        rs.getInt("storeIdx"),
                        storeImgUrl,
                        rs.getString("storeName"),
                        rs.getString("isCheetah"),
                        rs.getString("timeDelivery"),
                        rs.getString("isToGo"),
                        rs.getString("isCoupon"),
                        rs.getInt("minimumPrice"),
                        rs.getString("buildingName"),
                        rs.getString("storeAddress"),
                        rs.getString("storeAddressDetail"),
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
    public GetFavoriteListRes getFavoriteList(int userIdx, int storeIdx, UserLocation userLocation) {
        String StoreInfoQuery = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount,\n" +
                "       CASE WHEN S.isToGo='Y' THEN S.timeToGo ELSE 'N' END AS timeToGo,\n" +
                "       S.isToGo, S.isCoupon, S.status, S.minimumPrice, S.buildingName, S.storeAddress, S.storeAddressDetail,\n" +
                "       ROUND(ST_DISTANCE_SPHERE(POINT(S.storeLongitude,S.storeLatitude), POINT(?,?))*0.001,1) AS distance\n" +
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


        String MyOrderCountQuery = "SELECT COUNT(*)\n" +
                "FROM UserOrder\n" +
                "WHERE userIdx=? AND storeIdx=? AND status!='N' AND status!='F' AND status!='E';";
        String MyLatelyOrderTimeQuery = "SELECT orderTime\n" +
                "FROM UserOrder\n" +
                "WHERE userIdx=? AND storeIdx=? AND status!='N' AND status!='F' AND status!='E'\n" +
                "ORDER BY orderTime DESC\n" +
                "LIMIT 1;";
        String addFavoriteStoreTimeQuery ="SELECT DATE_FORMAT(createdAt, '%Y-%m-%d %H:%i:%s')\n" +
                "FROM Favorite\n" +
                "WHERE userIdx=? AND storeIdx=? AND status='Y';";

        int myOrderCount = this.jdbcTemplate.queryForObject(MyOrderCountQuery,
                int.class,
                userIdx, storeIdx);
        String latelyOrderTime = "";

        if (myOrderCount!=0){
            latelyOrderTime = this.jdbcTemplate.queryForObject(MyLatelyOrderTimeQuery,
                    String.class,
                    userIdx, storeIdx);
        }
        String myLatelyOrderTime = latelyOrderTime;



        return this.jdbcTemplate.queryForObject(StoreInfoQuery,
                (rs1, rowNum)-> new GetFavoriteListRes(
                        rs1.getInt("storeIdx"),
                        rs1.getString("storeImgUrl"),
                        rs1.getString("storeName"),
                        rs1.getString("isCheetah"),
                        rs1.getString("timeDelivery"),
                        rs1.getString("isToGo"),
                        rs1.getString("isCoupon"),
                        rs1.getDouble("distance"),
                        rs1.getString("status"),
                        rs1.getDouble("reviewScore"),
                        rs1.getInt("reviewCount"),
                        this.jdbcTemplate.queryForObject(DeliveryFeeQuery,
                                String.class,
                                storeIdx),
                        this.jdbcTemplate.queryForObject(CouponQuery,
                                String.class,
                                storeIdx),
                        myOrderCount,
                        myLatelyOrderTime,
                        this.jdbcTemplate.queryForObject(addFavoriteStoreTimeQuery,
                                String.class,
                                userIdx, storeIdx))
                , userLocation.getUserLongitude(), userLocation.getUserLatitude(), storeIdx);

    }

    /**
     * 가게별 리뷰 조회 API
     * [GET] /stores/review-list
     * /review-list?storeIdx=
     * @return BaseResponse<List<GetStoreReviewListRes>>
     */
    public GetStoreReviewListRes getStoreReviews(int userIdx, int storeIdx, StoreReviewIdx idx) {
    
        
        // 추가 여부 클라이언트와 상의
//        String ReviewStoreInfo = "SELECT S.storeIdx, S.storeName,R.reviewScore, R.reviewCount\n" +
//                "    FROM Store S\n" +
//                "    LEFT JOIN (\n" +
//                "        SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
//                "        FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx WHERE R.status='Y'\n" +
//                "        GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
//                "    WHERE S.status != 'N';";
//      
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

        String HelpedCountQuery = "SELECT COUNT(*) AS helpedCount\n" +
                "FROM ReviewLiked\n" +
                "WHERE reviewIdx=? AND isHelped='Y' AND status='Y';";

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

        String MyLikedQuery = "SELECT status\n" +
                "FROM ReviewLiked\n" +
                "WHERE userIdx=? AND reviewIdx=?;";

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

        System.out.println("bossReview");
        List<String> reviewImg = this.jdbcTemplate.query(MenuImgQuery,
                (rs, rowNum) -> { return rs.getString("reviewImgUrl");
                }, idx.getReviewIdx());
        System.out.println("reviewImg");

        int helpedCount = this.jdbcTemplate.queryForObject(HelpedCountQuery,
                int.class, idx.getReviewIdx());
        System.out.println("helpedCount");
        // 로그인 했을 경우 좋아요 여부 확인
        String myHelped = "N";
        System.out.println("userIdx"+userIdx+"idx.getReviewIdx"+idx.getReviewIdx());
        if (userIdx!=0){
            System.out.println("userIdx"+userIdx+"idx.getReviewIdx"+idx.getReviewIdx());
            if (this.jdbcTemplate.queryForObject("SELECT EXISTS(SELECT status FROM ReviewLiked WHERE userIdx=? AND reviewIdx=?);", int.class, userIdx, idx.getReviewIdx()) != 0){
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
                        helpedCount,
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
                "WHERE reviewIdx=? AND isHelped='Y' AND status='Y';";

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

        String InsertReviewInfoQuery = "INSERT INTO Review (userIdx, userOrderIdx, score, content, isPhoto, reasonForDelivery) VALUES(?,?,?,?,?,?);";
        String UpdateIsGood = "UPDATE CartToOrder SET isGood=?, reasonForMenu=? WHERE userIdx=? AND cartIdx=?";
        String InsertImage = "INSERT INTO ReviewImg (reviewIdx, reviewImgUrl);";
        String isPhoto;
        if (imageList.size()==0){
            isPhoto = "N";
        } else {
            isPhoto= "Y";
        }

        this.jdbcTemplate.update(InsertReviewInfoQuery, userIdx, userOrderIdx, postReviewReq.getScore(), postReviewReq.getContent(), isPhoto, postReviewReq.getReasonForDelivery());

        String lastInsertIdQuery = "select last_insert_id()";
        int reviewIdx = this.jdbcTemplate.queryForObject(lastInsertIdQuery,int.class);

        Set<Integer> keySet = postReviewReq.getReasonForMenu().keySet();
        Set<Integer> keySet2 = postReviewReq.getIsMenuGood().keySet();
        Iterator<Integer> keyIterator = keySet.iterator();
        Iterator<Integer> keyIterator2 = keySet2.iterator();
        while(keyIterator.hasNext()){
            int key = keyIterator.next();
            int key2 = keyIterator2.next();
            String value = postReviewReq.getReasonForMenu().get(key);
            String value2 = postReviewReq.getIsMenuGood().get(key2);
            this.jdbcTemplate.update(UpdateIsGood, value2, value, userIdx, key);
        }

        for (String url:imageList){
            this.jdbcTemplate.update(InsertImage, reviewIdx, url);
        }
        return 1;
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

    // 가게 카테고리 존재 여부 확인
    public int checkStoreCategory(int categoryIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM StoreCategory WHERE storeCategoryIdx=? AND status='Y');";
        int Param = categoryIdx;
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

    // 가게 idx 찾기
    public List<Integer> findStoreIdxList(int categoryIdx) {
        String Query = "SELECT storeIdx FROM Store WHERE status!='N';";

        String CategoryQuery = "SELECT S.storeIdx, SC.categoryName\n" +
                "FROM Store S\n" +
                "    JOIN StoreCategoryMapping SCM on S.storeIdx = SCM.storeIdx\n" +
                "    JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx\n" +
                "WHERE SCM.storeCategoryIdx=?;";

        if (categoryIdx==0){
            return this.jdbcTemplate.query(Query,
                    (rs, rowNum) -> {
                        return rs.getInt("storeIdx");
                    });
        }
        return this.jdbcTemplate.query(CategoryQuery,
                (rs, rowNum) -> {
                    return rs.getInt("storeIdx");
                }, categoryIdx);

    }

    // 즐겨찾기 한 가게 찾기
    public int checkFavoriteStore(int userIdx, int storeIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM Favorite WHERE userIdx=? AND storeIdx=? AND status='Y');";
        return this.jdbcTemplate.queryForObject(Query, int.class, userIdx, storeIdx);
    }


    // 즐겨찾기 한 가게 idx
    public List<Integer> getFavoriteStoreIdx(int userIdx) {
        String Query = "SELECT storeIdx FROM Favorite WHERE userIdx=? AND status='Y';";
        return this.jdbcTemplate.query(Query,
                (rs,rowNum) -> {
                    return rs.getInt("storeIdx");
                }, userIdx);
    }


    // 가게별 리뷰 idx
    public List<StoreReviewIdx> getStoreReviewIdx(int storeIdx) {
        String Query = "SELECT R.reviewIdx, R.userIdx, R.userOrderIdx\n" +
                "FROM Review R JOIN UserOrder UO on R.userOrderIdx = UO.userOrderIdx\n" +
                "WHERE UO.storeIdx=? AND R.status='Y';";
        return this.jdbcTemplate.query(Query,
                (rs, rowNum) -> new StoreReviewIdx(
                        rs.getInt("reviewIdx"),
                        rs.getInt("userOrderIdx"),
                        rs.getInt("userIdx")),
                storeIdx);
    }

    public int checkUserReview(int userIdx, int userOrderIdx) {
        String Query = "SELECT EXISTS(SELECT reviewIdx\n" +
                "FROM UserOrder UO JOIN Review R on UO.userOrderIdx = R.userOrderIdx\n" +
                "WHERE UO.userOrderIdx=? AND UO.userIdx=? AND R.status='Y');";
        return this.jdbcTemplate.queryForObject(Query, int.class, userOrderIdx, userIdx);
    }

}
