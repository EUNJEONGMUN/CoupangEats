package com.example.demo.src.store;

import com.example.demo.src.store.model.GetMenuImg;
import com.example.demo.src.store.model.Res.GetStoreHomeRes;
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
    public List<GetStoreHomeRes> getStoreHome() {
        String StoreInfoQuery = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery, R.reviewScore, R.reviewCount\n" +
                "FROM Store S\n" +
                "LEFT JOIN (\n" +
                "    SELECT UO.storeIdx, ROUND(AVG(R.score),1) AS reviewScore, COUNT(R.reviewIdx) AS reviewCount\n" +
                "    FROM Review R JOIN UserOrder UO on R.userOrderIdx=UO.userOrderIdx\n" +
                "    GROUP BY UO.storeIdx) R ON R.storeIdx=S.storeIdx\n" +
                "WHERE S.status!='N';";

        String StoreMenuImgQuery = "SELECT RankRow.storeIdx, RankRow.menuImgUrl\n" +
                "FROM (SELECT*, RANK() OVER (PARTITION BY M.storeIdX ORDER BY M.menuIdx) AS a\n" +
                "      FROM Menu M\n" +
                "     ) AS RankRow\n" +
                "WHERE RankRow.a <= 2 AND RankRow.storeIdx=?;";

        return this.jdbcTemplate.query(StoreInfoQuery,
                (rs1, rowNum1) -> new GetStoreHomeRes(
                        rs1.getString("storeImgUrl"),
                        rs1.getString("storeName"),
                        rs1.getString("isCheetah"),
                        rs1.getString("timeDelivery"),
                        rs1.getDouble("reviewScore"),
                        rs1.getInt("reviewCount"),
                        this.jdbcTemplate.query(StoreMenuImgQuery,
                                (rs2, rowNum2) -> new GetMenuImg(
                                        rs2.getString("menuImgUrl")
                                ), rs1.getInt("storeIdx"))

                ));
    }
}
