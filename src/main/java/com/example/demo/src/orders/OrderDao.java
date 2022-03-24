package com.example.demo.src.orders;

import com.example.demo.src.orders.model.CartMenu;
import com.example.demo.src.orders.model.Req.PostCreateCartReq;
import com.example.demo.src.orders.model.Res.GetCartListRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 배달 카트 담기 API
     * [POST] /order/cart
     * /cart?storeIdx=&menuIdx=
     * @return BaseResponse<String>
     */
    public int createCart(int userIdx, int storeIdx, int menuIdx, PostCreateCartReq postCreateCartReq) {
        String Query = "INSERT INTO Cart (userIdx, storeIdx, menuIdx, menuOptions, orderCount, orderPrice) VALUES(?,?,?,?,?,?);";
        Object[] Params = new Object[]{userIdx, storeIdx, menuIdx, postCreateCartReq.getMenuOptions(), postCreateCartReq.getOrderCount(), postCreateCartReq.getOrderPrice()};

        return this.jdbcTemplate.update(Query, Params);
    }

    /**
     * 배달 카트 새로 담기 API
     * [POST] /order/cart/new
     * /new?storeIdx=&menuIdx
     * @return BaseResponse<String>
     */
    public int deleteCartStore(int userIdx, int cartStoreIdx) {
        // 기존 가게 삭제
        String CartStoreDeleteQuery = "UPDATE Cart SET status='N' WHERE userIdx=? AND storeIdx=?;";
        Object[] Params = new Object[]{userIdx, cartStoreIdx};

        return this.jdbcTemplate.update(CartStoreDeleteQuery, Params);

    }

    /**
     * 배달 카트 담기 API - 같은 메뉴
     * [POST] /orders/cart
     * /cart?storeIdx=&menuIdx
     * @return BaseResponse<String>
     */
    public int addCart(int sameMenuCartIdx, PostCreateCartReq postCreateCartReq) {
        String GetNowOrderCount = "SELECT orderCount FROM Cart WHERE cartIdx=?;";
        int nowOrderCount = this.jdbcTemplate.queryForObject(GetNowOrderCount, int.class, sameMenuCartIdx);


        String UpdateCountQuery = "UPDATE Cart SET orderCount=? WHERE cartIdx=?;";
        int newOrderCount = nowOrderCount + postCreateCartReq.getOrderCount();
        Object[] Params = new Object[]{newOrderCount, sameMenuCartIdx};

        return this.jdbcTemplate.update(UpdateCountQuery, Params);
    }

    /**
     * 배달 카트 조회 API
     * [GET] /orders/cart-list
     * @return BaseResponse<GetCartListRes>
     */
    public GetCartListRes getCartList(int userIdx) {

        // 카트에 담긴 것이 있는지 확인
        String isCartQuery = "SELECT EXISTS(SELECT storeIdx FROM Cart WHERE status='Y' AND userIdx=?);";
        int isCart = this.jdbcTemplate.queryForObject(isCartQuery, int.class, userIdx);
        if (isCart==0){
            return new GetCartListRes();
        }

        // 가게 정보
        String findStoreIdx = "SELECT storeIdx FROM Cart WHERE status='Y' AND userIdx=? LIMIT 1;";
        int storeIdx = this.jdbcTemplate.queryForObject(findStoreIdx, int.class, userIdx);
        System.out.println("storeIdx>>"+storeIdx);

        String userInfoQuery = "";

        String storeInfoQuery = "SELECT S.storeIdx, S.storeName, S.isCheetah, S.timeDelivery,S.minimumPrice,\n" +
                "       CASE WHEN S.isToGo='Y' THEN S.timeToGo ELSE 'N' END AS timeToGo\n" +
                "FROM Store S\n" +
                "WHERE S.status != 'N' AND S.storeIdx=?;";

        String menuInfoQuery = "SELECT M.menuName, C.menuOptions, C.orderPrice, C.orderCount, C.orderPrice*C.orderCount AS mulPrice\n" +
                "FROM Cart C JOIN Menu M on C.menuIdx = M.menuIdx\n" +
                "WHERE C.userIdx=? AND C.status='Y';";



        String totalPriceQuery = "SELECT SUM(orderPrice*orderCount) AS mulPrice\n" +
                "FROM Cart\n" +
                "WHERE status='Y' AND userIdx=?\n" +
                "GROUP BY userIdx;";
        int totalPrice = this.jdbcTemplate.queryForObject(totalPriceQuery, int.class, userIdx);
        System.out.println("totalPrice>>"+totalPrice);

        String feeQuery = "SELECT MIN(deliveryFee) AS deliveryFee FROM DeliveryFee WHERE storeIdx=? AND (minPrice<=?<maxPrice OR minPrice<=?);";
        Object[] feeParams = new Object[]{storeIdx, totalPrice, totalPrice};
        int deliveryFee = this.jdbcTemplate.queryForObject(feeQuery, int.class, feeParams);
        System.out.println("deliveryFee>>"+deliveryFee);


        return this.jdbcTemplate.queryForObject(storeInfoQuery,
                (rs1, rowNum1) -> new GetCartListRes(
                        rs1.getString("storeName"),
                        rs1.getString("isCheetah"),
                        rs1.getInt("minimumPrice"),
                        rs1.getString("timeDelivery"),
                        rs1.getString("timeToGo"),
                        totalPrice,
                        deliveryFee,
                        this.jdbcTemplate.query(menuInfoQuery,
                                (rs3, rowNum3) -> new CartMenu(
                                        rs3.getString("menuName"),
                                        rs3.getString("menuOptions"),
                                        rs3.getInt("mulPrice"),
                                        rs3.getInt("orderCount")
                                ), userIdx)
                        )
                , storeIdx);

    }


    // 카트에 담겨진 가게 확인
    public int checkCartStore(int userIdx) {
        String existsStore = "SELECT EXISTS(SELECT * FROM Cart WHERE userIdx=? AND status='Y');";
        int isExistsStore = this.jdbcTemplate.queryForObject(existsStore, int.class, userIdx);

        if (isExistsStore == 0){
            return isExistsStore;
        }

        String Query ="SELECT storeIdx FROM Cart WHERE userIdx=? AND status='Y' LIMIT 1;";
        return this.jdbcTemplate.queryForObject(Query, int.class, userIdx);
    }

    // 배달 카트에 같은 메뉴+옵션 있는지 확인
    public Integer checkSameMenu(int userIdx, int menuIdx, String menuOptions) {
        String Query = "SELECT EXISTS(SELECT * FROM Cart WHERE userIdx=? AND menuIdx=? AND menuOptions=? AND status='Y');";
        Object[] Params = new Object[]{userIdx, menuIdx, menuOptions};
        int isSameMenu = this.jdbcTemplate.queryForObject(Query, int.class, Params);
        if (isSameMenu == 0){
            return isSameMenu;
        }
        String getCartIdxQuery = "SELECT cartIdx FROM Cart WHERE userIdx=? AND menuIdx=? AND menuOptions=? AND status='Y';";
        return this.jdbcTemplate.queryForObject(getCartIdxQuery, int.class, Params);

    }


}
