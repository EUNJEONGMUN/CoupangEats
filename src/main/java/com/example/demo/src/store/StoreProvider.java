package com.example.demo.src.store;

import com.example.demo.config.BaseException;
import com.example.demo.src.store.model.Res.GetStoreHomeRes;
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
    public List<GetStoreHomeRes> getStoreHome() throws BaseException {
        try {
            List<GetStoreHomeRes> getStoreHomeRes = storeDao.getStoreHome();
            return getStoreHomeRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
