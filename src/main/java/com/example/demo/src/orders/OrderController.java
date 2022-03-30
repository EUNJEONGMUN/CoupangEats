package com.example.demo.src.orders;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.orders.model.Req.*;
import com.example.demo.src.orders.model.Res.*;
import com.example.demo.src.store.StoreProvider;
import com.example.demo.src.user.UserProvider;
import com.example.demo.src.user.model.UserLocation;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

@RestController
@CrossOrigin(origins = "http://localhost:9009")
@RequestMapping("/orders")
public class OrderController {


    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final OrderProvider orderProvider;
    @Autowired
    private final OrderService orderService;
    @Autowired
    private final JwtService jwtService;
    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final StoreProvider storeProvider;

    public OrderController(OrderProvider orderProvider, OrderService orderService, JwtService jwtService, UserProvider userProvider, StoreProvider storeProvider){
        this.orderProvider = orderProvider;
        this.orderService = orderService;
        this.jwtService = jwtService;
        this.userProvider = userProvider;
        this.storeProvider = storeProvider;
    }

    /**
     * 배달 카트 담기 API
     * [POST] /orders/cart
     * /cart?storeIdx=&menuIdx
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/cart")
    public BaseResponse<String> createCart(@RequestParam(required = false, defaultValue = "0") int storeIdx,
                                           @RequestParam(required = false, defaultValue = "0") int menuIdx,
                                           @Valid @RequestBody PostCreateCartReq postCreateCartReq) throws BaseException{
        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        // orderCount 없을 경우 default값 1로 설정
        if (postCreateCartReq.getOrderCount() == 0){
            postCreateCartReq.setOrderCount(1);
        }

        // param 비었을 경우
        if (storeIdx==0 || menuIdx==0 ){
            return new BaseResponse<>(POST_CART_PARAM_EMPTY);
        }


        if (storeProvider.checkStore(storeIdx)==0){ // 가게가 없을 경우
            return new BaseResponse<>(EMPTY_STORE);
        }
        if (storeProvider.checkMenu(menuIdx)==0){ // 메뉴가 없을 경우
            return new BaseResponse<>(EMPTY_MENU);
        }
        if (storeProvider.checkMenuOwner(menuIdx)!=storeIdx){ // 메뉴가 속한 가게가 storeIdx와 일치하지 않을 경우
            return new BaseResponse<>(INCONSISTENCY_STORE_OWNER);
        }


        // 같은 가게가 아닐 경우
        int cartStoreIdx = orderProvider.checkCartStore(userIdx);
        if (cartStoreIdx!=0 && cartStoreIdx!=storeIdx){
            return new BaseResponse<>(CART_DUPLICATE_STORE);
        }
        // 배달 카트에 같은 메뉴+옵션 있는지 확인
        int sameMenuCartIdx = orderProvider.checkSameMenu(userIdx, menuIdx, postCreateCartReq.getMenuOptions());
        if (sameMenuCartIdx==0){
            // 카트에 같은 메뉴+옵션이 없다면
            orderService.createCart(userIdx, storeIdx, menuIdx, postCreateCartReq);
            String result = "";
            return new BaseResponse<>(result);
        }
        // 카트에 같은 메뉴+옵션이 있다면
        orderService.addCart(sameMenuCartIdx, postCreateCartReq);
        String result = "";
        return new BaseResponse<>(result);




    }

    /**
     * 배달 카트 새로 담기 API
     * [POST] /orders/cart/new
     * /new?storeIdx=&menuIdx
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/cart/new")
    public BaseResponse<String> createCartNew(@RequestParam(required = false, defaultValue = "0") int storeIdx,
                                           @RequestParam(required = false, defaultValue = "0") int menuIdx,
                                           @Valid @RequestBody PostCreateCartReq postCreateCartReq) throws BaseException{

        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        // orderCount 없을 경우 default값 1로 설정
        if (postCreateCartReq.getOrderCount() == 0){
            postCreateCartReq.setOrderCount(1);
        }

        // param 비었을 경우
        if (storeIdx==0 || menuIdx==0 ){
            return new BaseResponse<>(POST_CART_PARAM_EMPTY);
        }

        if (storeProvider.checkStore(storeIdx)==0){ // 가게가 없을 경우
            return new BaseResponse<>(EMPTY_STORE);
        }
        if (storeProvider.checkMenu(menuIdx)==0){ // 메뉴가 없을 경우
            return new BaseResponse<>(EMPTY_MENU);
        }
        if (storeProvider.checkMenuOwner(menuIdx)!=storeIdx){ // 메뉴가 속한 가게가 storeIdx와 일치하지 않을 경우
            return new BaseResponse<>(INCONSISTENCY_STORE_OWNER);
        }


        int cartStoreIdx = orderProvider.checkCartStore(userIdx);

        // 같은 가게이거나 없을 경우
        if (cartStoreIdx==storeIdx || cartStoreIdx==0) {
            return new BaseResponse<>(CART_NOT_DUPLICATE_STORE);
        }

        orderService.createCartNew(userIdx, storeIdx, menuIdx, cartStoreIdx, postCreateCartReq);
        String result = "";
        return new BaseResponse<>(result);
    }

    /**
     * 배달 카트 조회 API
     * [GET] /orders/cart-list
     * @return BaseResponse<GetCartListRes>
     */
    @ResponseBody
    @GetMapping("/cart-list")
    public BaseResponse<GetCartListRes> getCartList() throws BaseException {
        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        // 사용자가 현재 설정한 주소 확인
        if (orderProvider.checkUserNowAddress(userIdx) == 0){
            // 없다면 주소 설정해달라는 오류 반환
            return new BaseResponse<>(USER_NOW_ADDRESS_NOT_EXISTS);
        }
        UserLocation userLocation = storeProvider.getNowUserLocation(userIdx);
        GetCartListRes getCartListRes = orderProvider.getCartList(userIdx, userLocation);
        return new BaseResponse<>(getCartListRes);

    }

    /**
     * 배달 카트 수정 API
     * [PUT] /orders/cart
     * /cart?storeIdx=&cardIdx=
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PutMapping("/cart")
    public BaseResponse<String> modifyCart(@RequestParam(required = false, defaultValue = "0") int storeIdx,
                                           @RequestParam(required = false, defaultValue = "0") int cartIdx,
                                           @Valid @RequestBody PutModifyCartReq putModifyCartReq) throws BaseException {

        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        if (storeIdx==0 || cartIdx==0){
            return new BaseResponse<>(PUT_CART_PARAM_EMPTY);
        }

//        if (putModifyCartReq.getStatus()==null){
//            putModifyCartReq.setStatus("Y");
//        }

        String result = "";
        orderService.modifyCart(storeIdx, cartIdx, putModifyCartReq);
        return new BaseResponse<>(result);


    }

    /**
     * 배달 카트 삭제 API
     * [PATCH] /orders/cart/deletion
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/cart/deletion")
    public BaseResponse<String> deleteCart(@RequestBody PatchCartReq patchCartReq) throws BaseException {
        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        // 카트 사용자 확인
        if(orderProvider.checkCartUser(userIdx, patchCartReq.getCartIdx())==0){
            return new BaseResponse<>(INCONSISTENCY_CART_USER);
        }

        String result = "";
        orderService.deleteCart(userIdx, patchCartReq.getCartIdx());
        return new BaseResponse<>(result);

    }



    /**
     * 주문하기 API
     * [POST] /orders/delivery
     * /delivery?cartList=
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/delivery")
    public BaseResponse<PostCreateOrderRes> createOrder(@RequestParam String[] cartList, @RequestBody PostCreateOrderReq postCreateOrderReq) throws BaseException {
        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }
        System.out.println(postCreateOrderReq.getCouponIdx());

        if (postCreateOrderReq.getIsSpoon()==null){
            postCreateOrderReq.setIsSpoon("N");
        }
        if (storeProvider.checkStore(postCreateOrderReq.getStoreIdx())==0){ // 가게가 없을 경우
            return new BaseResponse<>(EMPTY_STORE);
        }
        int userOrderIdx = orderService.createOrder(userIdx, cartList, postCreateOrderReq);

        return new BaseResponse<>(new PostCreateOrderRes(userOrderIdx));

    }

    /**
     * 주문취소 API
     * [PUT] /orders/delivery/status?userOrderIdx=
     * /status?userOrderIdx=
     * @return BaseResponse<PutOrderRes>
     */
    @ResponseBody
    @PutMapping("/delivery/status")
    public BaseResponse<String> deleteOrder(@RequestParam int userOrderIdx) throws BaseException {
        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        // 주문 존재 여부 확인
        if (orderProvider.checkOrder(userOrderIdx)==0){
            return new BaseResponse<>(USER_ORDER_NOT_EXISTS);
        }

        if (!orderProvider.checkUserOrderStatus(userOrderIdx)){
            return new BaseResponse<>(USER_ORDER_ALREADY_DELETED);
        }

        // 주문 소유자 확인
        if (orderProvider.checkOrderOwner(userIdx, userOrderIdx)==0){
            return new BaseResponse<>(INCONSISTENCY_ORDER_USER);
        }

        String result = "";
        orderService.deleteOrder(userIdx, userOrderIdx);
        return new BaseResponse<>(result);

    }

    /**
     * 주문조회 API
     * [GET] /orders/delivery-list
     * @return BaseResponse<List<GetDeliveryRes>>
     */
    @ResponseBody
    @GetMapping("/delivery-list")
    public BaseResponse<List<GetDeliveryListRes>> getUserDelivery() throws BaseException {
        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        // 카트 내역 존재 여부 확인
        // 구현하기???

        List<GetDeliveryListRes> getDeliveryListRes = orderProvider.getUserDelivery(userIdx);
        return new BaseResponse<>(getDeliveryListRes);
    }


    /**
     * 재주문 하기 API
     * [POST] /orders/delivery/reorder?userOrderIdx=
     * /new?userOrderIdx=
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/delivery/reorder")
    public BaseResponse<String> reCreateOrderDefault(@RequestParam(required = false, defaultValue = "0") int userOrderIdx) throws BaseException {

        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        if (userOrderIdx==0){
            return new BaseResponse<>(EMPTY_USER_ORDER_IDX_PARAM);
        }

        // 주문 소유자 확인
        if (orderProvider.checkOrderOwner(userIdx, userOrderIdx)==0){
            return new BaseResponse<>(INCONSISTENCY_ORDER_USER);
        }

        // 주문 상태 확인
        if (!orderProvider.checkUserOrderStatus(userOrderIdx)){
            return new BaseResponse<>(INCONSISTENCY_STORE_STATE);
        }

        int cartStoreIdx = orderProvider.checkCartStore(userIdx);
        // 카트에 메뉴가 있는지 확인
        if (cartStoreIdx!=0){
            return new BaseResponse<>(ALREADY_CART_STORE);
        }


        orderService.reCreateOrderDefault(userOrderIdx);
        String result = "";
        return new BaseResponse<>(result);

    }


    /**
     * 재주문 하기 API
     * [POST] /orders/delivery/reorder/new?userOrderIdx=
     * /new?userOrderIdx=
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/delivery/reorder/new")
    public BaseResponse<String> reCreateOrder(@RequestParam(required = false, defaultValue = "0") int userOrderIdx) throws BaseException {

        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        if (userOrderIdx==0){
            return new BaseResponse<>(EMPTY_USER_ORDER_IDX_PARAM);
        }

        // 주문 소유자 확인
        if (orderProvider.checkOrderOwner(userIdx, userOrderIdx)==0){
            return new BaseResponse<>(INCONSISTENCY_ORDER_USER);
        }

        // 주문 상태 확인
        if (!orderProvider.checkUserOrderStatus(userOrderIdx)){
            return new BaseResponse<>(INCONSISTENCY_STORE_STATE);
        }

        int cartStoreIdx = orderProvider.checkCartStore(userIdx);


        orderService.reCreateOrder(userIdx, userOrderIdx, cartStoreIdx);
        String result = "";
        return new BaseResponse<>(result);
    }


}
