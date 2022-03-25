package com.example.demo.src.store;

import com.example.demo.src.store.model.*;
import com.example.demo.src.store.model.Res.GetStoreDetailRes;
import com.example.demo.src.store.model.Res.GetStoreHomeRes;
import com.example.demo.src.store.model.Res.GetStoreMenuOptionsRes;
import com.example.demo.src.user.model.UserLocation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
    public GetStoreHomeRes getStoreHome(UserLocation userLocation) {

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
                "                    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "                    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "                WHERE S.status != 'N';";

        String DeliveryTimeQuery = "SELECT storeIdx, minPrice, maxPrice, deliveryFee FROM DeliveryFee;";

        String StoreCategoryQuery = "SELECT SCM.storeIdx, SC.storeCategoryIdx, categoryName\n" +
                "FROM StoreCategoryMapping SCM JOIN StoreCategory SC on SCM.storeCategoryIdx = SC.storeCategoryIdx;";

        String StoreCouponQuery = "SELECT couponIdx, storeIdx, couponTitle, discountPrice, limitPrice, endDate, couponType, DATE_FORMAT(createdAt, '%Y-%m-%d %H:%I:%S') AS createdAt, status FROM Coupon WHERE status='Y';";

        String orderCountQuery = "SELECT UO.storeIdx, COUNT(UO.userOrderIdx) AS orderCount\n" +
                "FROM UserOrder UO\n" +
                "GROUP BY UO.storeIdx";

        String StoreMenuImgQuery = "SELECT RankRow.storeIdx, RankRow.menuImgUrl\n" +
                "FROM (SELECT*, RANK() OVER (PARTITION BY M.storeIdX ORDER BY M.menuIdx) AS a\n" +
                "      FROM Menu M\n" +
                "     ) AS RankRow\n" +
                "WHERE RankRow.a <= 2;";

        List<DeliveryFeeInfo> deliveryFeeInfo = this.jdbcTemplate.query(DeliveryTimeQuery,
                (rs1, rowNum1) -> new DeliveryFeeInfo(
                        rs1.getInt("storeIdx"),
                        rs1.getInt("minPrice"),
                        rs1.getInt("maxPrice"),
                        rs1.getInt("deliveryFee")
                ));

        List<StoreCategory> storeCategory = this.jdbcTemplate.query(StoreCategoryQuery,
                (rs2, rowNum2) -> new StoreCategory(
                        rs2.getInt("storeIdx"),
                        rs2.getInt("storeCategoryIdx"),
                        rs2.getString("categoryName")
                ));

        List<StoreCouponInfo> storeCouponInfo = this.jdbcTemplate.query(StoreCouponQuery,
                (rs2, rowNum2) -> new StoreCouponInfo(
                        rs2.getInt("couponIdx"),
                        rs2.getInt("storeIdx"),
                        rs2.getString("couponTitle"),
                        rs2.getInt("discountPrice"),
                        rs2.getInt("limitPrice"),
                        rs2.getString("endDate"),
                        rs2.getString("couponType"),
                        rs2.getString("createdAt"),
                        rs2.getString("status")
                ));
        Object[] Params = new Object[]{userLocation.getUserLongitude(), userLocation.getUserLatitude()};
        List<StoreInfo> storeInfo = this.jdbcTemplate.query(StoreInfoQuery,
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
                        rs1.getDouble("distance"))
        , Params);

        List<OrderCount> orderCount = this.jdbcTemplate.query(orderCountQuery,
                (rs1, rowNum1) -> new OrderCount(
                        rs1.getInt("storeIdx"),
                        rs1.getInt("orderCount"))
        );

        List<StoreMenuImg> storeMenuImg = this.jdbcTemplate.query(StoreMenuImgQuery,
                (rs1, rowNum1) -> new StoreMenuImg(
                        rs1.getInt("storeIdx"),
                        rs1.getString("menuImgUrl"))
        );

        return new GetStoreHomeRes(storeInfo,deliveryFeeInfo, storeCategory, storeCouponInfo ,orderCount,storeMenuImg);
    }

    /**
     * 가게 상세 화면 조회 조회 API
     * [GET] /stores/detail?storeIdx=
     * @return BaseResponse<List<GetStoreHomeRes>>
     */
    public GetStoreDetailRes getStoreDetail(int storeIdx) {
//        String StoreInfoQuery = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount, F.fee, S.minimumPrice,\n" +
//                "       CASE WHEN S.isToGo='Y' THEN S.timeToGo ELSE 'N' END AS timeToGo, S.storeLongitude, S.storeLatitude\n" +
//                "FROM Store S\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
//                "    FROM DeliveryFee\n" +
//                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
//                "LEFT JOIN (\n" +
//                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
//                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
//                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
//                "WHERE S.status != 'N' AND S.storeIdx=?;";

        String StoreInfoQuery = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount, CASE WHEN S.isToGo='Y' THEN S.timeToGo ELSE 'N' END AS timeToGo,\n" +
                "       S.isToGo, S.isCoupon, S.status, S.minimumPrice\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status != 'N' AND S.storeIdx=?;";

//        String StoreCouponQuery = "SELECT S.storeIdx, IFNULL(C.discountPrice,0) AS maxDiscountPrice, IFNULL(C.couponType,'N') AS couponType\n" +
//                "FROM Store S\n" +
//                "LEFT JOIN (SELECT RankRow.storeIdx, RankRow.discountPrice, RankRow.couponType\n" +
//                "            FROM (SELECT*, RANK() OVER (PARTITION BY storeIdX ORDER BY discountPrice DESC, couponIdx ASC) AS a\n" +
//                "                  FROM Coupon\n" +
//                "                WHERE status='Y' AND  DATEDIFF(endDate, CURRENT_DATE())>=0\n" +
//                "                 ) AS RankRow\n" +
//                "            WHERE RankRow.a <= 1) C ON C.storeIdx = S.storeIdx\n" +
//                "WHERE S.storeIdx = ?;";
        String StoreCouponQuery = "SELECT couponIdx, storeIdx, couponTitle, discountPrice, limitPrice, endDate, couponType, DATE_FORMAT(createdAt, '%Y-%m-%d %H:%I:%S') AS createdAt, status FROM Coupon WHERE status='Y' AND storeIdx=?;";
        String DeliveryTimeQuery = "SELECT storeIdx, minPrice, maxPrice, deliveryFee FROM DeliveryFee WHERE storeIdx=?;";
        String MenuCategoryQuery = "SELECT MC.menuCategoryIdx, storeIdx, categoryName\n" +
                "FROM MenuCategory MC\n" +
                "WHERE MC.storeIdx=?;";

        String MenuDetailQuery = "SELECT menuName, menuPrice, menuDetail, menuImgUrl, isOption, status\n" +
                "FROM Menu\n" +
                "WHERE status!='N' AND menuCategoryIdx=?;";

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
                "    WHERE R.isPhoto='Y' AND R.status='Y') PhotoReview ON PhotoReview.userOrderIdx = UO.userOrderIdx\n" +
                "WHERE UO.status='Y' AND UO.storeIdx=?;";


        int Param = storeIdx;

        List<PhotoReview> photoReview = this.jdbcTemplate.query(PhotoReviewQuery,
                (rs1, rowNum1) -> new PhotoReview(
                        rs1.getInt("reviewIdx"),
                        rs1.getString("reviewImgUrl"),
                        rs1.getString("content"),
                        rs1.getInt("score"),
                        rs1.getString("createdAt")
                ), Param);

        List<MenuCategory> menuCategory = this.jdbcTemplate.query(MenuCategoryQuery,
                (rs3, rowNum3) -> new MenuCategory(
                        rs3.getString("categoryName"),
                        this.jdbcTemplate.query(MenuDetailQuery,
                                (rs4, rowNum4) -> new MenuDetail(
                                        rs4.getString("menuName"),
                                        rs4.getInt("menuPrice"),
                                        rs4.getString("menuDetail"),
                                        rs4.getString("menuImgUrl"),
                                        rs4.getString("isOption"),
                                        rs4.getString("status")
                                ), rs3.getInt("menuCategoryIdx"))
                ), Param);

        List<DeliveryFeeInfo> deliveryFeeInfo = this.jdbcTemplate.query(DeliveryTimeQuery,
                (rs1, rowNum1) -> new DeliveryFeeInfo(
                        rs1.getInt("storeIdx"),
                        rs1.getInt("minPrice"),
                        rs1.getInt("maxPrice"),
                        rs1.getInt("deliveryFee")
                ), Param);
        List<StoreCouponInfo> storeCouponInfo = this.jdbcTemplate.query(StoreCouponQuery,
                (rs2, rowNum2) -> new StoreCouponInfo(
                        rs2.getInt("couponIdx"),
                        rs2.getInt("storeIdx"),
                        rs2.getString("couponTitle"),
                        rs2.getInt("discountPrice"),
                        rs2.getInt("limitPrice"),
                        rs2.getString("endDate"),
                        rs2.getString("couponType"),
                        rs2.getString("createdAt"),
                        rs2.getString("status")
                ), Param);

        return this.jdbcTemplate.queryForObject(StoreInfoQuery,
                (rs, rowNum) -> new GetStoreDetailRes(
                        rs.getInt("storeIdx"),
                        rs.getString("storeImgUrl"),
                        rs.getString("storeName"),
                        rs.getString("isCheetah"),
                        rs.getString("timeDelivery"),
                        rs.getString("isToGo"),
                        rs.getString("isCoupon"),
                        rs.getInt("minimumPrice"),
                        rs.getString("status"),
                        rs.getDouble("reviewScore"),
                        rs.getInt("reviewCount"),
                        rs.getString("timeToGo"),
                        storeCouponInfo,
                        deliveryFeeInfo,
                        photoReview,
                        menuCategory),
                Param);


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

        int Param = menuIdx;

        return this.jdbcTemplate.queryForObject(MenuInfoQuery,
                (rs1, rowNum1) -> new GetStoreMenuOptionsRes(
                        rs1.getString("menuImgUrl"),
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
}
