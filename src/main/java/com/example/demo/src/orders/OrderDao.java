package com.example.demo.src.orders;

import com.example.demo.src.orders.model.*;
import com.example.demo.src.orders.model.Req.PostCreateCartReq;
import com.example.demo.src.orders.model.Req.PostCreateOrderReq;
import com.example.demo.src.orders.model.Req.PutModifyCartReq;
import com.example.demo.src.orders.model.Res.GetCartListRes;
import com.example.demo.src.orders.model.Res.GetDeliveryListRes;
import com.example.demo.src.store.model.DeliveryFeeInfo;
import com.example.demo.src.user.model.AddressInfo;
import com.example.demo.src.user.model.UserLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
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
    public GetCartListRes getCartList(int userIdx, UserLocation userLocation) {

        // 카트에 담긴 것이 있는지 확인
        String isCartQuery = "SELECT EXISTS(SELECT storeIdx FROM Cart WHERE status='Y' AND userIdx=?);";
        int isCart = this.jdbcTemplate.queryForObject(isCartQuery, int.class, userIdx);
        if (isCart==0){
            return new GetCartListRes();
        }

        // 사용자의 현재 위치
        String nowQuery = "SELECT userAddressIdx, buildingName, address, addressDetail, addressGuide, addressTitle, addressLongitude, addressLatitude, addressType, isNowLocation\n" +
                "                FROM UserAddress\n" +
                "                WHERE userIdx=? AND isNowLocation='Y' AND status='Y';";


        // 가게 정보
        String findStoreIdx = "SELECT storeIdx FROM Cart WHERE status='Y' AND userIdx=? LIMIT 1;";
        int storeIdx = this.jdbcTemplate.queryForObject(findStoreIdx, int.class, userIdx);
        System.out.println("storeIdx>>"+storeIdx);


        String storeInfoQuery = "SELECT S.storeIdx, S.storeImgUrl,S.storeName, S.isCheetah, S.timeDelivery,\n" +
                "    S.isToGo, S.isCoupon, S.status, S.minimumPrice, S.buildingName, S.storeAddress, S.storeAddressDetail,\n" +
                "       CASE WHEN S.isToGo='Y' THEN S.timeToGo ELSE 'N' END AS timeToGo, S.storeLongitude,S.storeLatitude,\n" +
                "    ROUND(ST_DISTANCE_SPHERE(POINT(S.storeLongitude,S.storeLatitude), POINT(?,?))*0.001,1) AS distance\n" +
                "FROM Store S\n" +
                "WHERE S.status != 'N' AND S.storeIdx=?;";

        String menuInfoQuery = "SELECT C.cartIdx, M.menuName, C.menuOptions, C.orderPrice, C.orderCount, C.orderPrice*C.orderCount AS mulPrice\n" +
                "FROM Cart C JOIN Menu M on C.menuIdx = M.menuIdx\n" +
                "WHERE C.userIdx=? AND C.status='Y';";



        String totalPriceQuery = "SELECT SUM(orderPrice*orderCount) AS mulPrice\n" +
                "FROM Cart\n" +
                "WHERE status='Y' AND userIdx=?\n" +
                "GROUP BY userIdx;";
        int totalPrice = this.jdbcTemplate.queryForObject(totalPriceQuery, int.class, userIdx);
        System.out.println("totalPrice>>"+totalPrice);

//        String feeQuery = "SELECT MIN(deliveryFee) AS deliveryFee FROM DeliveryFee WHERE storeIdx=? AND (minPrice<=?<maxPrice OR minPrice<=?);";
//        Object[] feeParams = new Object[]{storeIdx, totalPrice, totalPrice};
//        int deliveryFee = this.jdbcTemplate.queryForObject(feeQuery, int.class, feeParams);

        String DeliveryFeeQuery = "SELECT storeIdx, minPrice, maxPrice, deliveryFee FROM DeliveryFee WHERE storeIdx=?;";

        // 사용자의 현재 위치 정보
        CartAddressInfo  nowAddress = this.jdbcTemplate.queryForObject(nowQuery,
                    (rs, rowNum) -> new CartAddressInfo(
                            rs.getInt("userAddressIdx"),
                            rs.getString("buildingName"),
                            rs.getString("address"),
                            rs.getString("addressDetail"),
                            rs.getString("addressGuide"),
                            rs.getDouble("addressLongitude"),
                            rs.getDouble("addressLatitude"),
                            rs.getString("addressTitle"),
                            rs.getString("addressType")
                    ), userIdx);


        return this.jdbcTemplate.queryForObject(storeInfoQuery,
                (rs1, rowNum1) -> new GetCartListRes(
                        nowAddress,
                        storeIdx,
                        rs1.getString("storeName"),
                        rs1.getString("isCheetah"),
                        rs1.getInt("minimumPrice"),
                        rs1.getString("timeDelivery"),
                        rs1.getString("timeToGo"),
                        rs1.getString("status"),
                        rs1.getString("buildingName"),
                        rs1.getString("storeAddress"),
                        rs1.getString("storeAddressDetail"),
                        rs1.getDouble("distance"),
                        rs1.getDouble("storeLongitude"),
                        rs1.getDouble("storeLatitude"),
                        totalPrice,
                        this.jdbcTemplate.query(DeliveryFeeQuery,
                                (rs2, rowNum2) -> new DeliveryFeeList(
                                        rs2.getInt("minPrice"),
                                        rs2.getInt("maxPrice"),
                                        rs2.getInt("deliveryFee")
                                ), storeIdx),
                        this.jdbcTemplate.query(menuInfoQuery,
                                (rs3, rowNum3) -> new CartMenu(
                                        rs3.getInt("cartIdx"),
                                        rs3.getString("menuName"),
                                        rs3.getString("menuOptions"),
                                        rs3.getInt("mulPrice"),
                                        rs3.getInt("orderCount")
                                ), userIdx)
                        )
                , userLocation.getUserLongitude(), userLocation.getUserLatitude(), storeIdx);

    }

    // 사용자가 설정한 주소가 있는지 확인
    public int checkUserNowAddress(int userIdx){
        String Query = "SELECT EXISTS(SELECT * FROM UserAddress WHERE userIdx=? AND isNowLocation='Y' AND status='Y');";
        return this.jdbcTemplate.queryForObject(Query, int.class, userIdx);
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
     * 배달 카트 수정 API
     * [PUT] /orders/cart/status
     * /status?storeIdx=&cardIdx=
     * @return BaseResponse<String>
     */
    public int modifyCart(int storeIdx, int cartIdx, PutModifyCartReq putModifyCartReq) {
        String Query = "UPDATE Cart SET orderCount=? WHERE cartIdx=? AND storeIdx=?;";
        Object[] Params = new Object[]{putModifyCartReq.getChangeCount(), cartIdx, storeIdx};
        return this.jdbcTemplate.update(Query, Params);
    }

    /**
     * 배달 카트 삭제 API
     * [PATCH] /orders/cart/deletion
     * @return BaseResponse<String>
     */
    public int deleteCart(int userIdx, int cartIdx) {
        String Query = "UPDATE Cart SET status='N' WHERE cartIdx=? AND userIdx=?;";
        return this.jdbcTemplate.update(Query, cartIdx, userIdx);
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
        String InsertUserOrderQuery = "INSERT INTO UserOrder (userIdx, storeIdx, message, deliveryManOptionIdx, deliveryManContent, useCouponIdx, orderTime, userAddressIdx) VALUES (?,?,?,?,?,?,?,?);";

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
                postCreateOrderReq.getDeliveryManOptionIdx(), postCreateOrderReq.getDeliveryManContent(),
                postCreateOrderReq.getCouponIdx(), orderTime, postCreateOrderReq.getUserAddressIdx()};
        this.jdbcTemplate.update(InsertUserOrderQuery,Params2); // 주문테이블
        String lastInsertIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery,int.class);

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
        String UpdateUserOrder = "UPDATE UserOrder SET status='E' WHERE userOrderIdx=?;";
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

        if (orderInfo.getUseCouponIdx()!=0){
            Object[] ParamsCoupon = new Object[]{userIdx, orderInfo.getUseCouponIdx()};
            this.jdbcTemplate.update(UpdateCoupon, ParamsCoupon);
        }
        this.jdbcTemplate.update(UpdateUserOrder, userOrderIdx);
        return this.jdbcTemplate.update(UpdateCartToOrder, Params);
    }

    /**
     * 주문조회 API
     * [GET] /orders/delivery-list
     * @return BaseResponse<List<GetDeliveryRes>>
     */
    public GetDeliveryListRes getUserDelivery(int userIdx, OrderList orderList) {

        System.out.println(">>>>"+orderList.getStoreIdx()+"<<<   >>>" + orderList.getUserOrderIdx()+"<<<   >>>" + orderList.getOrderTime());
        String StoreInfo = "SELECT storeIdx, storeName, storeImgUrl\n" +
                "FROM Store\n" +
                "WHERE storeIdx=?";

        String TotalPriceQuery = "SELECT CONCAT(FORMAT(SUM(C.orderPrice),0),'원') AS totalPrice\n" +
                "FROM Cart C JOIN (\n" +
                "    SELECT CTO.userIdx, CTO.cartIdx, UO.orderTime, CTO.isGood, UO.status\n" +
                "    FROM UserOrder UO JOIN CartToOrder CTO on UO.orderTime = CTO.orderTime\n" +
                "    WHERE UO.userIdx=? AND UO.orderTime=? AND UO.status!='N'\n" +
                "    ORDER BY UO.orderTime DESC) OrderMenu ON OrderMenu.cartIdx = C.cartIdx;";

        // 배달 상태

//        String OrderStatus = "SELECT CASE WHEN UO.status='A' THEN '주문 수락됨'\n" +
//                "           WHEN UO.status='B' THEN '메뉴 준비중'\n" +
//                "           WHEN UO.status='C' THEN '배달중'\n" +
//                "           WHEN UO.status='D' THEN '배달 완료'\n" +
//                "           ELSE UO.status\n" +
//                "               END AS status,\n" +
//                "       CASE\n" +
//                "        WHEN INSTR(DATE_FORMAT(UO.orderTime, '%Y-%m-%d %p %h:%i'), 'PM') > 0\n" +
//                "        THEN REPLACE(DATE_FORMAT(UO.orderTime, '%Y-%m-%d %p %h:%i'), 'PM', '오후')\n" +
//                "        ELSE REPLACE(DATE_FORMAT(UO.orderTime, '%Y-%m-%d %p %h:%i'), 'AM', '오전')\n" +
//                "        END AS orderTime\n" +
//                "FROM UserOrder UO\n" +
//                "WHERE UO.userOrderIdx=?";

        // 테스트 환경에서는 실제로 배달이 이루어 지지 않으므로 시간차를 둠.
        String OrderStatus = "SELECT CASE WHEN TIMESTAMPDIFF(SECOND, UO.orderTime, CURRENT_TIMESTAMP())<30 THEN '주문 수락됨'\n" +
                "           WHEN TIMESTAMPDIFF(SECOND, UO.orderTime, CURRENT_TIMESTAMP())<60 THEN '메뉴 준비중'\n" +
                "            WHEN TIMESTAMPDIFF(SECOND, UO.orderTime, CURRENT_TIMESTAMP())<90 THEN '배달중'\n" +
                "            WHEN UO.status='E' THEN '고객 주문취소'\n" +
                "            WHEN UO.status='F' THEN '매장 주문취소'\n" +
                "        ELSE '배달 완료'\n" +
                "            END AS status\n" +
                "       ,\n" +
                "       CASE\n" +
                "        WHEN INSTR(DATE_FORMAT(UO.orderTime, '%Y-%m-%d %p %h:%i'), 'PM') > 0\n" +
                "        THEN REPLACE(DATE_FORMAT(UO.orderTime, '%Y-%m-%d %p %h:%i'), 'PM', '오후')\n" +
                "        ELSE REPLACE(DATE_FORMAT(UO.orderTime, '%Y-%m-%d %p %h:%i'), 'AM', '오전')\n" +
                "        END AS orderTime\n" +
                "FROM UserOrder UO\n" +
                "WHERE UO.userOrderIdx=?\n";

        String isReview = "SELECT EXISTS(SELECT score FROM Review WHERE userOrderIdx=?);";
        String ReviewScore = "SELECT score\n" +
                "FROM Review\n" +
                "WHERE userOrderIdx=?;";


        String OrderMenuInfoQuery = "SELECT M.menuName, C.menuOptions, C.orderCount, OrderMenu.cartIdx, OrderMenu.isGood\n" +
                "FROM Cart C JOIN (\n" +
                "        SELECT CTO.userIdx, CTO.cartIdx, UO.orderTime, CTO.isGood, UO.status\n" +
                "        FROM UserOrder UO JOIN CartToOrder CTO on UO.orderTime = CTO.orderTime\n" +
                "        WHERE UO.userIdx=? AND UO.orderTime=? AND UO.status!='N'\n" +
                "        ORDER BY UO.orderTime DESC) OrderMenu ON OrderMenu.cartIdx = C.cartIdx\n" +
                "JOIN Menu M on C.menuIdx = M.menuIdx;\n";

        Object[] Params = new Object[]{userIdx, orderList.getOrderTime()};

        int review = 0;
        // 리뷰를 했다면
        if (this.jdbcTemplate.queryForObject(isReview, int.class, orderList.getUserOrderIdx()) != 0){
            review = this.jdbcTemplate.queryForObject(ReviewScore,
                    int.class,
                    orderList.getUserOrderIdx());
        }
        int reviewScore = review;



        OrderStatus orderStatus = this.jdbcTemplate.queryForObject(OrderStatus,
                (rs, rowNum) -> new OrderStatus(
                        rs.getString("status"),
                        rs.getString("orderTime")
                ), orderList.getUserOrderIdx());

        String totalPrice = this.jdbcTemplate.queryForObject(TotalPriceQuery,
                String.class,
                Params);


        int userAddressIdx = this.jdbcTemplate.queryForObject("SELECT userAddressIdx FROM UserOrder WHERE userOrderIdx=?", int.class, orderList.getUserOrderIdx());


        String UserAddressQuery = "SELECT userAddressIdx, buildingName, address, addressDetail, addressGuide, addressTitle, addressLongitude, addressLatitude, addressType, isNowLocation\n" +
                "                FROM UserAddress\n" +
                "                WHERE userAddressIdx=?;";

        CartAddressInfo  userDeliveryAddress = this.jdbcTemplate.queryForObject(UserAddressQuery,
                (rs, rowNum) -> new CartAddressInfo(
                        rs.getInt("userAddressIdx"),
                        rs.getString("buildingName"),
                        rs.getString("address"),
                        rs.getString("addressDetail"),
                        rs.getString("addressGuide"),
                        rs.getDouble("addressLongitude"),
                        rs.getDouble("addressLatitude"),
                        rs.getString("addressTitle"),
                        rs.getString("addressType")
                ), userAddressIdx);

        System.out.println(orderList.getStoreIdx());
        return this.jdbcTemplate.queryForObject(StoreInfo,
                (rs, rowNum) -> new GetDeliveryListRes(
                        userDeliveryAddress,
                        orderList.getUserOrderIdx(),
                        rs.getInt("storeIdx"),
                        rs.getString("storeImgUrl"),
                        rs.getString("storeName"),
                        orderStatus.getOrderTime(),
                        orderStatus.getStatus(),
                        totalPrice,
                        reviewScore,
                        this.jdbcTemplate.query(OrderMenuInfoQuery,
                                (rs2, rowNum2) -> new OrderMenuInfo(
                                        rs2.getInt("cartIdx"),
                                        rs2.getInt("orderCount"),
                                        rs2.getString("menuName"),
                                        rs2.getString("menuOptions"),
                                        rs2.getString("isGood")
                                ),Params)
                ), orderList.getStoreIdx());

    }


    /**
     * 재주문하기 API
     * [POST] /orders/delivery/reorder?userOrderIdx=
     * /reorder?userOrderIdx=
     * @return BaseResponse<String>
     */
    public int reCreateOrder(int userOrderIdx) {
        String SelectCartIdxQuery = "SELECT cartIdx\n" +
                "FROM UserOrder UO JOIN CartToOrder CTO on UO.orderTime = CTO.orderTime\n" +
                "WHERE UO.userOrderIdx=?;";

        String InsertCart = "INSERT INTO Cart (userIdx, storeIdx, menuIdx, menuOptions, orderCount, orderPrice)\n" +
                "SELECT userIdx, storeIdx, menuIdx, menuOptions, orderCount, orderPrice FROM Cart WHERE cartIdx = ?;";

        List<Integer> cartIdxList = this.jdbcTemplate.query(SelectCartIdxQuery,
                (rs, rowNum) -> { return rs.getInt("cartIdx");}, userOrderIdx);

        for (int idx:cartIdxList){
            this.jdbcTemplate.update(InsertCart, idx);
        }
        return 1;

    }
    // 주문 존재 여부 확인
    public int checkOrder(int userOrderIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM UserOrder WHERE userOrderIdx=? AND status!='N');";
        return this.jdbcTemplate.queryForObject(Query, int.class, userOrderIdx);
    }

    // 주문 소유자 확인
    public int checkOrderOwner(int userIdx, int userOrderIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM UserOrder WHERE userOrderIdx=? AND userIdx=?);";
        Object[] Params = new Object[]{userOrderIdx, userIdx};
        return this.jdbcTemplate.queryForObject(Query, int.class, Params);
    }

    // orderTime 찾기
    public List<OrderList> findOrderList(int userIdx) {
        String Query = "SELECT orderTime, storeIdx, userOrderIdx\n" +
                "FROM UserOrder\n" +
                "WHERE userIdx=? AND status!='N'\n" +
                "ORDER BY orderTime DESC;";

        return this.jdbcTemplate.query(Query,
                (rs, rowNum) -> new OrderList(
                        rs.getInt("storeIdx"),
                        rs.getString("orderTime"),
                        rs.getInt("userOrderIdx")
                ), userIdx);
    }

    // 주문 상태 확인
    public boolean checkUserOrderStatus(int userOrderIdx) {
        String Query = "SELECT status FROM UserOrder WHERE userOrderIdx=?;";
        String userOrderStatus = this.jdbcTemplate.queryForObject(Query, String.class, userOrderIdx);
        if (userOrderStatus.equals("E")||userOrderStatus.equals("F")){
            return false;
        }
        return true;


    }

    public int findStoreIdx(int userOrderIdx) {
        String Query = "SELECT storeIdx FROM UserOrder WHERE userOrderIdx=?";
        return this.jdbcTemplate.queryForObject(Query, int.class, userOrderIdx);
    }

    // 카트의 사용자 확인
    public int checkCartUser(int userIdx, int cartIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM Cart WHERE cartIdx=? AND userIdx=? AND status='Y');";
        return this.jdbcTemplate.queryForObject(Query, int.class, cartIdx, userIdx);
    }

    public int checkCartStoreOwner(int cartIdx) {
        String Query = "SELECT storeIdx FROM Cart WHERE cartIdx=?;";
        return this.jdbcTemplate.queryForObject(Query, int.class, cartIdx);

    }

    // 카트 존재여부 확인
    public int checkCartExists(int cartIdx) {
        String Query = "SELECT EXISTS(SELECT*FROM Cart WHERE cartIdx=? AND status='Y');";
        return this.jdbcTemplate.queryForObject(Query, int.class, cartIdx);
    }


}
