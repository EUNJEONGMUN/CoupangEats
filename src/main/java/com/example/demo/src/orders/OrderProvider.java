package com.example.demo.src.orders;

import com.example.demo.config.BaseException;
import com.example.demo.src.orders.model.OrderList;
import com.example.demo.src.orders.model.Res.GetCartListRes;
import com.example.demo.src.orders.model.Res.GetDeliveryListRes;
import com.example.demo.src.user.model.UserLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public GetCartListRes getCartList(int userIdx, UserLocation userLocation) throws BaseException {
        try {
            GetCartListRes getCartListRes = orderDao.getCartList(userIdx, userLocation);
            return getCartListRes;
        } catch (Exception exception) {
            System.out.println("getCartList-> "+ exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 주문조회 API
     * [GET] /orders/delivery-list
     * @return BaseResponse<List<GetDeliveryRes>>
     */
    public List<GetDeliveryListRes> getUserDelivery(int userIdx) throws BaseException {
        try {
            List<OrderList> orderList = orderDao.findOrderList(userIdx);
            List<GetDeliveryListRes> getDeliveryListRes = new ArrayList<>();

            for (OrderList userOrder:orderList){
                GetDeliveryListRes userOrderList = orderDao.getUserDelivery(userIdx, userOrder);
                getDeliveryListRes.add(userOrderList);
            }
            return getDeliveryListRes;
        } catch (Exception exception) {
            System.out.println("getUserDelivery-> "+ exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

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

    // 사용자가 현재 설정한 주소 확인
    public int checkUserNowAddress(int userIdx) throws BaseException {
        try{
            return orderDao.checkUserNowAddress(userIdx);
        } catch (Exception exception) {
            System.out.println("checkUserNowAddress-> "+ exception);
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

    // 주문 상태 확인
    public boolean checkUserOrderStatus(int userOrderIdx) throws BaseException {
        try {
            return orderDao.checkUserOrderStatus(userOrderIdx);
        } catch (Exception exception) {
            System.out.println("checkUserOrderStatus"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // storeIdx 가져오기
    public int findStoreIdx(int userOrderIdx) throws BaseException {
        try {
            return orderDao.findStoreIdx(userOrderIdx);
        } catch (Exception exception) {
            System.out.println("findStoreIdx"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 카트의 사용자 확인
    public int checkCartUser(int userIdx, int cartIdx) throws BaseException {
        try {
            return orderDao.checkCartUser(userIdx, cartIdx);
        } catch (Exception exception) {
            System.out.println("checkCartUser"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 카트에 담긴 가게 확인
    public int checkCartStoreOwner(int cartIdx) throws BaseException {
        try {
            return orderDao.checkCartStoreOwner(cartIdx);
        } catch (Exception exception) {
            System.out.println("checkCartStoreOwner"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 카트 존재여부 확인
    public int checkCartExists(int cartIdx) throws BaseException {
        try {
            return orderDao.checkCartExists(cartIdx);
        } catch (Exception exception) {
            System.out.println("checkCartExists"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }


}
