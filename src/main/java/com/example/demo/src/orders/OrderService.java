package com.example.demo.src.orders;

import com.example.demo.config.BaseException;
import com.example.demo.src.orders.model.Req.PostCreateCartReq;
import com.example.demo.src.orders.model.Req.PostCreateOrderReq;
import com.example.demo.src.orders.model.Req.PutModifyCartReq;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.demo.config.BaseResponseStatus.*;

@Service
public class OrderService {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final OrderDao orderDao;
    private final OrderProvider orderProvider;
    private final JwtService jwtService;
    private final int FAIL = 0;

    @Autowired
    public OrderService(OrderDao orderDao, OrderProvider orderProvider, JwtService jwtService){
        this.orderDao = orderDao;
        this.jwtService = jwtService;
        this.orderProvider = orderProvider;

    }

    /**
     * 배달 카트 담기 API
     * [POST] /order/cart
     * /cart?storeIdx=&menuIdx=
     * @return BaseResponse<String>
     */
    public void createCart(int userIdx, int storeIdx, int menuIdx, PostCreateCartReq postCreateCartReq) throws BaseException {
        try {
            int result = orderDao.createCart(userIdx, storeIdx, menuIdx, postCreateCartReq);
            if (result == FAIL){
                throw new BaseException(FAIL_CREATE_CART);
            }
        } catch (Exception exception) {
            System.out.println("createCart"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 배달 카트 담기 API - 같은 메뉴
     * [POST] /orders/cart
     * /cart?storeIdx=&menuIdx
     * @return BaseResponse<String>
     */
    public void addCart(int sameMenuCartIdx, PostCreateCartReq postCreateCartReq) throws BaseException {
        try {
            int result = orderDao.addCart(sameMenuCartIdx, postCreateCartReq);
            if (result == FAIL){
                throw new BaseException(FAIL_CREATE_CART);
            }
        } catch (Exception exception) {
            System.out.println("addCart"+exception);
            throw new BaseException(DATABASE_ERROR);
        }


    }
    /**
     * 배달 카트 새로 담기 API
     * [POST] /order/cart/new
     * /new?storeIdx=&menuIdx
     * @return BaseResponse<String>
     */
    public void createCartNew(int userIdx, int storeIdx, int menuIdx, int cartStoreIdx, PostCreateCartReq postCreateCartReq) throws BaseException {
        try {
            int deleteCart = orderDao.deleteCartStore(userIdx, cartStoreIdx);
            if (deleteCart == FAIL){
                throw new BaseException(FAIL_DELETE_CART_STORE);
            }
            int result = orderDao.createCart(userIdx, storeIdx, menuIdx, postCreateCartReq);
            if (result == FAIL){
                throw new BaseException(FAIL_CREATE_CART);
            }
        } catch (Exception exception) {
            System.out.println("createCartNew"+exception);
            throw new BaseException(DATABASE_ERROR);
        }

    }

    /**
     * 배달 카트 수정 삭제 API
     * [PUT] /orders/cart/status
     * /status?storeIdx=&cardIdx=
     * @return BaseResponse<String>
     */
    public void modifyCart(int storeIdx, int cartIdx, PutModifyCartReq putModifyCartReq) throws BaseException {
        try {
            int result = orderDao.modifyCart(storeIdx, cartIdx, putModifyCartReq);
            if (result == FAIL){
                throw new BaseException(FAIL_MODIFY_CART);
            }
        }catch (Exception exception) {
            System.out.println("modifyCart"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 주문하기 API
     * [POST] /orders/delivery
     * /delivery?cartList=
     * @return BaseResponse<String>
     */
    public void createOrder(int userIdx, String[] cartList, PostCreateOrderReq postCreateOrderReq) throws BaseException {
        try {

            if (postCreateOrderReq.getCouponIdx()!=0){
                // 쿠폰을 사용한다면 -> 사용 처리
                orderDao.userCoupon(userIdx, postCreateOrderReq.getCouponIdx());
            }

            int result = orderDao.createOrder(userIdx, cartList, postCreateOrderReq);
            if (result == FAIL){
                throw new BaseException(FAIL_CREATE_ORDER);
            }
        }catch (Exception exception) {
            System.out.println("createOrder"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * 주문취소 API
     * [PUT] /orders/delivery/status?userOrderIdx=
     * /status?userOrderIdx=
     * @return BaseResponse<PutOrderRes>
     */
    public void deleteOrder(int userIdx, int userOrderIdx) throws BaseException {
        try {
            int result = orderDao.deleteOrder(userIdx, userOrderIdx);
            if (result == FAIL){
                throw new BaseException(FAIL_DELETE_ORDER);
            }
        } catch (Exception exception) {
                System.out.println("checkOrder"+exception);
                throw new BaseException(DATABASE_ERROR);
            }
    }

    // 주문 존재 여부 확인
    public int checkOrder(int userOrderIdx) throws BaseException {
        try {
            return orderDao.checkOrder(userOrderIdx);
        }catch (Exception exception) {
            System.out.println("checkOrder"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 주문 소유자 확인
    public int checkOrderOwner(int userIdx, int userOrderIdx) throws BaseException {
        try {
            return orderDao.checkOrderOwner(userIdx, userOrderIdx);
        }catch (Exception exception) {
            System.out.println("checkOrderOwner"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
