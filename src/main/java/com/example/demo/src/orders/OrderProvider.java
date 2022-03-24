package com.example.demo.src.orders;

import com.example.demo.config.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.demo.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class OrderProvider {

    private final OrderDao orderDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public OrderProvider(OrderDao orderDao) {this.orderDao = orderDao; }

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
