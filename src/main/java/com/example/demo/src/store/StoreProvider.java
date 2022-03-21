package com.example.demo.src.store;

import com.example.demo.config.BaseException;
import com.example.demo.src.store.model.Res.GetStoreDetailRes;
import com.example.demo.src.store.model.Res.GetStoreHomeRes;
import com.example.demo.src.store.model.StoreHome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

@Service
public class StoreProvider {
    private final StoreDao storeDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public StoreProvider(StoreDao storeDao) {this.storeDao = storeDao; }

    /**
     * 홈 화면 조회 API
     * [GET] /stores/home
     * @return BaseResponse<List<GetStoreHomeRes>>
     */
    public List<GetStoreHomeRes> getStoreHome(StoreHome storeHome) throws BaseException {
        try {
            List<GetStoreHomeRes> getStoreHomeRes = storeDao.getStoreHome(storeHome);
            return getStoreHomeRes;
        } catch (Exception exception) {
            System.out.println("storehome-> "+ exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 가게 상세 화면 조회 조회 API
     * [GET] /stores/detail?storeIdx=
     * @return BaseResponse<List<GetStoreHomeRes>>
     */
    public GetStoreDetailRes getStoreDetail(int storeIdx) throws BaseException {
        try {

            // 쿠폰 있을 때랑 없을 때 나누기

            GetStoreDetailRes getStoreDetailRes = storeDao.getStoreDetail(storeIdx);
            return getStoreDetailRes;
        } catch (Exception exception) {
            System.out.println("store_detail-> "+ exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 가게 존재 여부 확인
    public int checkStore(int storeIdx) throws BaseException{
        try {
            return storeDao.checkStore(storeIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
