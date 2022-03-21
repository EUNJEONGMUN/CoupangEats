package com.example.demo.src.store;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.UnAuth;
import com.example.demo.src.store.model.StoreHome;
import com.example.demo.src.store.model.Res.GetStoreHomeRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores")
public class StoreController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final StoreProvider storeProvider;
    @Autowired
    private final StoreService storeService;

    public StoreController(StoreProvider storeProvider, StoreService storeService){
        this.storeProvider = storeProvider;
        this.storeService = storeService;
    }

    /**
     * 홈 화면 조회 API
     * [GET] /stores/home
     * @return BaseResponse<List<GetStoreHomeRes>>
     */
    @UnAuth
    @ResponseBody
    @GetMapping("/home")
    public BaseResponse<List<GetStoreHomeRes>> getStoreHome(StoreHome storeHome) throws BaseException{
        List<GetStoreHomeRes> getStoreHomeRes = storeProvider.getStoreHome(storeHome);
        return new BaseResponse<>(getStoreHomeRes);
    }

}
