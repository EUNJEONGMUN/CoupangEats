package com.example.demo.src.orders;

import com.example.demo.config.BaseException;
import com.example.demo.src.orders.model.Res.GetCartListRes;
import com.example.demo.src.orders.model.Res.GetDeliveryListRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class OrderProvider {

    private final OrderDao orderDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public OrderProvider(OrderDao orderDao) {this.orderDao = orderDao; }


    /**
     * 배달 카트 조회 API
     * [GET] /orders/cart-list
     * @return BaseResponse<List<GetCartListRes>>
     */
    public GetCartListRes getCartList(int userIdx) throws BaseException {
        try {
            GetCartListRes getCartListRes = orderDao.getCartList(userIdx);
            return getCartListRes;
        } catch (Exception exception) {
            System.out.println("getCartList-> "+ exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

//    /**
//     * 주문조회 API
//     * [GET] /orders/delivery-list
//     * @return BaseResponse<List<GetDeliveryRes>>
//     */
//    public List<GetDeliveryListRes> getUserDelivery(int userIdx) throws BaseException {
//        try {
//            List<GetDeliveryListRes> getDeliveryListRes = orderDao.getUserDelivery(userIdx);
//            return getDeliveryListRes;
//        } catch (Exception exception) {
//            System.out.println("getUserDelivery-> "+ exception);
//            throw new BaseException(DATABASE_ERROR);
//        }
//    }

    // 카드에 담겨진 가게 확인
    public int checkCartStore(int userIdx) throws BaseException {
        try{
            return orderDao.checkCartStore(userIdx);
        } catch (Exception exception) {
            System.out.println("checkCartStore-> "+ exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 배달 카트에 같은 메뉴+옵션 있는지 확인
    public int checkSameMenu(int userIdx, int menuIdx, String menuOptions) throws BaseException {
        try{
            return orderDao.checkSameMenu(userIdx, menuIdx, menuOptions);
        } catch (Exception exception) {
            System.out.println("checkSameMenu-> "+ exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }


}
