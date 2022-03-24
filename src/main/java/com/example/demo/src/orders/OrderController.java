package com.example.demo.src.orders;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.orders.model.Req.PostCreateCartReq;
import com.example.demo.src.orders.model.Res.GetCartListRes;
import com.example.demo.src.user.UserProvider;
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

    public OrderController(OrderProvider orderProvider, OrderService orderService, JwtService jwtService, UserProvider userProvider){
        this.orderProvider = orderProvider;
        this.orderService = orderService;
        this.jwtService = jwtService;
        this.userProvider = userProvider;
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

        // 같은 가게가 아닐 경우
        if (!(orderProvider.checkCartStore(userIdx)==storeIdx)){
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
    @PutMapping("/cart/new")
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
     * @return BaseResponse<List<GetCartListRes>>
     */
    @ResponseBody
    @GetMapping("/cart-list")
    public BaseResponse<GetCartListRes> getCartList() throws BaseException {
        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        GetCartListRes getCartListRes = orderProvider.getCartList(userIdx);
        return new BaseResponse<>(getCartListRes);

    }



}
