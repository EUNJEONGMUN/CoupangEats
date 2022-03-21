package com.example.demo.src.store;

import com.example.demo.src.store.model.MenuCategory;
import com.example.demo.src.store.model.MenuDetail;
import com.example.demo.src.store.model.Res.GetStoreDetailRes;
import com.example.demo.src.store.model.Res.GetStoreHomeRes;
import com.example.demo.src.store.model.StoreCouponInfo;
import com.example.demo.src.store.model.StoreHome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

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
    public List<GetStoreHomeRes> getStoreHome(StoreHome storeHome) {

        // 주문 많은 순
        String Query1 = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount, F.fee, S.isToGo, S.storeLongitude, S.storeLatitude\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, COUNT(UO.userOrderIdx) AS orderCount\n" +
                "    FROM UserOrder UO\n" +
                "    GROUP BY UO.storeIdx) OC ON OC.storeIdx=S.storeIdx\n" +
                "WHERE S.status != 'N' AND  (S.isCheetah=? OR S.isCheetah=?) AND (S.isToGo=? OR S.isToGo=?) AND (S.isCoupon=? OR S.isCoupon=?) AND F.fee <= ? AND S.minimumPrice <=?\n" +
                "ORDER BY OC.orderCount DESC;";

        // 별점 높은 순
        String Query2 = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount, F.fee, S.isToGo, S.storeLongitude, S.storeLatitude\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status != 'N' AND  (S.isCheetah=? OR S.isCheetah=?) AND (S.isToGo=? OR S.isToGo=?) AND (S.isCoupon=? OR S.isCoupon=?) AND F.fee <= ? AND S.minimumPrice <=?\n" +
                "ORDER BY R.reviewScore DESC;";

        // 신규 매장 순
        String Query3 = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount, F.fee, S.isToGo, S.storeLongitude, S.storeLatitude\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status != 'N' AND  (S.isCheetah=? OR S.isCheetah=?) AND (S.isToGo=? OR S.isToGo=?) AND (S.isCoupon=? OR S.isCoupon=?) AND F.fee <= ? AND S.minimumPrice <=?\n" +
                "ORDER BY S.createdAt DESC;";

        String StoreCouponQuery = "SELECT S.storeIdx, IFNULL(C.discountPrice,0) AS maxDiscountPrice, IFNULL(C.couponType,'N') AS couponType\n" +
                "FROM Store S\n" +
                "LEFT JOIN (SELECT RankRow.storeIdx, RankRow.discountPrice, RankRow.couponType\n" +
                "            FROM (SELECT*, RANK() OVER (PARTITION BY storeIdX ORDER BY discountPrice DESC, couponIdx ASC) AS a\n" +
                "                  FROM Coupon\n" +
                "                WHERE status='Y' AND  DATEDIFF(endDate, CURRENT_DATE())>=0\n" +
                "                 ) AS RankRow\n" +
                "            WHERE RankRow.a <= 1) C ON C.storeIdx = S.storeIdx\n" +
                "WHERE S.storeIdx = ?;";

        String StoreMenuImgQuery = "SELECT RankRow.storeIdx, RankRow.menuImgUrl\n" +
                "FROM (SELECT*, RANK() OVER (PARTITION BY M.storeIdX ORDER BY M.menuIdx) AS a\n" +
                "      FROM Menu M\n" +
                "     ) AS RankRow\n" +
                "WHERE RankRow.a <= 2 AND RankRow.storeIdx=?;";




        if (storeHome.getSort()==null){storeHome.setSort("default");}
        if (storeHome.getIsCheetah()==null){storeHome.setIsCheetah("N");}
        if (storeHome.getFee()==0){storeHome.setFee(5000);}
        if (storeHome.getMinimum()==0){storeHome.setMinimum(100000);}
        if (storeHome.getIsToGo()==null){storeHome.setIsToGo("N");}
        if (storeHome.getIsCoupon()==null){storeHome.setIsCoupon("N");}

        Object[] Params = new Object[]{storeHome.getIsCheetah(),"Y", storeHome.getIsToGo(),"Y", storeHome.getIsCoupon(),"Y", storeHome.getFee(), storeHome.getMinimum()};

        if (storeHome.getSort().equals("score")){
            return this.jdbcTemplate.query(Query2,
                (rs1, rowNum1) -> new GetStoreHomeRes(
                        rs1.getString("storeImgUrl"),
                        rs1.getString("storeName"),
                        rs1.getString("isCheetah"),
                        rs1.getString("timeDelivery"),
                        rs1.getDouble("reviewScore"),
                        rs1.getInt("reviewCount"),
                        rs1.getInt("fee"),
                        rs1.getString("isToGo"),
                        rs1.getDouble("storeLongitude"),
                        rs1.getDouble("storeLatitude"),
                        this.jdbcTemplate.queryForObject(StoreCouponQuery,
                                (rs2, rowNum2) -> new StoreCouponInfo(
                                        rs2.getInt("maxDiscountPrice"),
                                        rs2.getString("couponType")
                                ), rs1.getInt("storeIdx")),
                        this.jdbcTemplate.query(StoreMenuImgQuery,
                                (rs3, rowNum3) -> new String(
                                        rs3.getString("menuImgUrl")
                                ), rs1.getInt("storeIdx"))

                ), Params);

        } else if (storeHome.getSort().equals("new")){
            return this.jdbcTemplate.query(Query3,
                    (rs1, rowNum1) -> new GetStoreHomeRes(
                            rs1.getString("storeImgUrl"),
                            rs1.getString("storeName"),
                            rs1.getString("isCheetah"),
                            rs1.getString("timeDelivery"),
                            rs1.getDouble("reviewScore"),
                            rs1.getInt("reviewCount"),
                            rs1.getInt("fee"),
                            rs1.getString("isToGo"),
                            rs1.getDouble("storeLongitude"),
                            rs1.getDouble("storeLatitude"),
                            this.jdbcTemplate.queryForObject(StoreCouponQuery,
                                    (rs2, rowNum2) -> new StoreCouponInfo(
                                            rs2.getInt("maxDiscountPrice"),
                                            rs2.getString("couponType")
                                    ), rs1.getInt("storeIdx")),
                            this.jdbcTemplate.query(StoreMenuImgQuery,
                                    (rs2, rowNum2) -> new String(
                                            rs2.getString("menuImgUrl")
                                    ), rs1.getInt("storeIdx"))

                    ), Params);

        }
        System.out.println("default");
        return this.jdbcTemplate.query(Query1,
                (rs1, rowNum1) -> new GetStoreHomeRes(
                        rs1.getString("storeImgUrl"),
                        rs1.getString("storeName"),
                        rs1.getString("isCheetah"),
                        rs1.getString("timeDelivery"),
                        rs1.getDouble("reviewScore"),
                        rs1.getInt("reviewCount"),
                        rs1.getInt("fee"),
                        rs1.getString("isToGo"),
                        rs1.getDouble("storeLongitude"),
                        rs1.getDouble("storeLatitude"),
                        this.jdbcTemplate.queryForObject(StoreCouponQuery,
                                (rs2, rowNum2) -> new StoreCouponInfo(
                                        rs2.getInt("maxDiscountPrice"),
                                        rs2.getString("couponType")
                                ), rs1.getInt("storeIdx")),
                        this.jdbcTemplate.query(StoreMenuImgQuery,
                                (rs2, rowNum2) -> new String(
                                        rs2.getString("menuImgUrl")
                                ), rs1.getInt("storeIdx"))

                ), Params);

    }

    /**
     * 가게 상세 화면 조회 조회 API
     * [GET] /stores/detail?storeIdx=
     * @return BaseResponse<List<GetStoreHomeRes>>
     */
    public GetStoreDetailRes getStoreDetail(int storeIdx) {
        String StoreInfoQuery = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount, F.fee, S.minimumPrice,\n" +
                "       CASE WHEN S.isToGo='Y' THEN S.timeToGo ELSE 'N' END AS timeToGo, S.storeLongitude, S.storeLatitude\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT StoreIdx, IFNULL(MIN(deliveryFee),0) AS fee\n" +
                "    FROM DeliveryFee\n" +
                "    WHERE DeliveryFee.status='Y' GROUP BY storeIdx) F ON F.storeIdx = S.storeIdx\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status != 'N' AND S.storeIdx=?;";

        String StoreCouponQuery = "SELECT S.storeIdx, IFNULL(C.discountPrice,0) AS maxDiscountPrice, IFNULL(C.couponType,'N') AS couponType\n" +
                "FROM Store S\n" +
                "LEFT JOIN (SELECT RankRow.storeIdx, RankRow.discountPrice, RankRow.couponType\n" +
                "            FROM (SELECT*, RANK() OVER (PARTITION BY storeIdX ORDER BY discountPrice DESC, couponIdx ASC) AS a\n" +
                "                  FROM Coupon\n" +
                "                WHERE status='Y' AND  DATEDIFF(endDate, CURRENT_DATE())>=0\n" +
                "                 ) AS RankRow\n" +
                "            WHERE RankRow.a <= 1) C ON C.storeIdx = S.storeIdx\n" +
                "WHERE S.storeIdx = ?;";

        String MenuCategoryQuery = "SELECT MC.menuCategoryIdx, storeIdx, categoryName\n" +
                "FROM MenuCategory MC\n" +
                "WHERE MC.storeIdx=?;";

        String MenuDetailQuery = "SELECT menuName, menuPrice, menuDetail, menuImgUrl, isOption\n" +
                "FROM Menu\n" +
                "WHERE Menu.menuCategoryIdx=?;";

        int Param = storeIdx;

        return this.jdbcTemplate.queryForObject(StoreInfoQuery,
                (rs1, rowNum1) -> new GetStoreDetailRes(
                        rs1.getString("storeImgUrl"),
                        rs1.getString("storeName"),
                        rs1.getString("isCheetah"),
                        rs1.getString("timeDelivery"),
                        rs1.getDouble("reviewScore"),
                        rs1.getInt("reviewCount"),
                        rs1.getInt("fee"),
                        rs1.getInt("minimumPrice"),
                        rs1.getString("timeToGo"),
                        rs1.getDouble("storeLongitude"),
                        rs1.getDouble("storeLatitude"),
                        this.jdbcTemplate.queryForObject(StoreCouponQuery,
                                (rs2, rowNum2) -> new StoreCouponInfo(
                                        rs2.getInt("maxDiscountPrice"),
                                        rs2.getString("couponType")
                                ), Param),
                        this.jdbcTemplate.query(MenuCategoryQuery,
                                (rs3, rowNum3) -> new MenuCategory(
                                        rs3.getString("categoryName"),
                                        this.jdbcTemplate.query(MenuDetailQuery,
                                                (rs4, rowNum4) -> new MenuDetail(
                                                        rs4.getString("menuName"),
                                                        rs4.getInt("menuPrice"),
                                                        rs4.getString("menuDetail"),
                                                        rs4.getString("menuImgUrl")
                                                ), rs3.getInt("menuCategoryIdx"))
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
}
