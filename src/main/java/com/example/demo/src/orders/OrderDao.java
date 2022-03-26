package com.example.demo.src.orders;

import com.example.demo.src.orders.model.CartMenu;
import com.example.demo.src.orders.model.OrderInfo;
import com.example.demo.src.orders.model.Req.PostCreateCartReq;
import com.example.demo.src.orders.model.Req.PostCreateOrderReq;
import com.example.demo.src.orders.model.Req.PutModifyCartReq;
import com.example.demo.src.orders.model.Res.GetCartListRes;
import com.example.demo.src.orders.model.Res.GetDeliveryListRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    /**
     * 배달 카트 수정 삭제 API
     * [PUT] /orders/cart/status
     * /status?storeIdx=&cardIdx=
     * @return BaseResponse<String>
     */
    public int modifyCart(int storeIdx, int cartIdx, PutModifyCartReq putModifyCartReq) {
        String Query = "UPDATE Cart SET orderCount=?, status=? WHERE cartIdx=? AND storeIdx=?;";
        Object[] Params = new Object[]{putModifyCartReq.getChangeCount(), putModifyCartReq.getStatus(), cartIdx, storeIdx};
        return this.jdbcTemplate.update(Query, Params);
    }


    /**
     * 주문하기 API
     * [POST] /orders/delivery
     * /delivery?cartList=
     * @return BaseResponse<String>
     */
    public int createOrder(int userIdx, String[] cartList, PostCreateOrderReq postCreateOrderReq) {
        String InsertCartToOrderQuery = "INSERT INTO CartToOrder (userIdx, cartIdx, orderTime) VALUES (?,?,?);";
        String UpdateUserCart = "UPDATE Cart SET status='O' WHERE cartIdx=?;";
        String InsertUserOrderQuery = "INSERT INTO UserOrder (userIdx, storeIdx, message, deliveryManOptionIdx, deliveryManContent, useCouponIdx, orderTime) VALUES (?,?,?,?,?,?,?);";

        // 현재 날짜 구하기
        LocalDateTime now = LocalDateTime.now();

        // 포맷 정의
        String orderTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // 포맷 적용

        for (int i=0; i< cartList.length; i++){
            Object[] Params = new Object[]{userIdx, cartList[i], orderTime};
            this.jdbcTemplate.update(InsertCartToOrderQuery, Params); // 주문 매핑 테이블
            this.jdbcTemplate.update(UpdateUserCart, cartList[i]); // 카트에서 삭제
        }

        Object[] Params2 = new Object[]{userIdx, postCreateOrderReq.getStoreIdx(), postCreateOrderReq.getMessage(),
                postCreateOrderReq.getDeliveryManOptionIdx(), postCreateOrderReq.getDeliveryManContent(), postCreateOrderReq.getCouponIdx(), orderTime};
        return this.jdbcTemplate.update(InsertUserOrderQuery,Params2); // 주문테이블

    }

    // 쿠폰 사용 처리
    public void userCoupon(int userIdx, int couponIdx) {
        String Query = "UPDATE UserCoupon SET status='U' WHERE couponIdx=? AND userIdx=?;";
        Object[] Params = new Object[]{couponIdx, userIdx};
        this.jdbcTemplate.update(Query, Params);
    }

    /**
     * 주문취소 API
     * [PUT] /orders/delivery/status?userOrderIdx=
     * /status?userOrderIdx=
     * @return BaseResponse<PutOrderRes>
     */
    public int deleteOrder(int userIdx, int userOrderIdx) {
        String UpdateUserOrder = "UPDATE UserOrder SET status='N' WHERE userOrderIdx=?;";
        String GetOrderInfo = "SELECT orderTime, useCouponIdx FROM UserOrder WHERE userOrderIdx=?;";
        String UpdateCartToOrder = "UPDATE CartToOrder SET status='N' WHERE userIdx=? AND orderTime=?;";
        String UpdateCoupon = "UPDATE UserCoupon SET status='Y' WHERE userIdx=? AND userCouponIdx=?;";


        // 사용한 쿠폰 정보와 주문 시간 select
        OrderInfo orderInfo = this.jdbcTemplate.queryForObject(GetOrderInfo,
                (rs, rowNum) -> new OrderInfo(
                        rs.getString("orderTime"),
                        rs.getInt("useCouponIdx"))
                , userOrderIdx);

        Object[] Params = new Object[]{userIdx, orderInfo.getOrderTime()};
        Object[] ParamsCoupon = new Object[]{userIdx, orderInfo.getUseCouponIdx()};

        this.jdbcTemplate.update(UpdateUserOrder, userOrderIdx);
        this.jdbcTemplate.update(UpdateCartToOrder, Params);
        return this.jdbcTemplate.update(UpdateCoupon, ParamsCoupon);
    }

//    /**
//     * 주문조회 API
//     * [GET] /orders/delivery-list
//     * @return BaseResponse<List<GetDeliveryRes>>
//     */
//    public List<GetDeliveryListRes> getUserDelivery(int userIdx) {
//
//    }
    // 주문 존재 여부 확인
    public int checkOrder(int userOrderIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM UserOrder WHERE userOrderIdx=? AND status='Y');";
        return this.jdbcTemplate.queryForObject(Query, int.class, userOrderIdx);
    }

    // 주문 소유자 확인
    public int checkOrderOwner(int userIdx, int userOrderIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM UserOrder WHERE userOrderIdx=? AND userIdx=?);";
        Object[] Params = new Object[]{userOrderIdx, userIdx};
        return this.jdbcTemplate.queryForObject(Query, int.class, Params);
    }

}
