package com.example.demo.src.store;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.UnAuth;
import com.example.demo.src.store.model.Res.GetStoreDetailRes;
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

    /**
     * 가게 상세 화면 조회 조회 API
     * [GET] /stores/detail?storeIdx=
     * @return BaseResponse<GetStoreDetailRes>
     */
    @UnAuth
    @ResponseBody
    @GetMapping("/detail")
    public BaseResponse<GetStoreDetailRes> getStoreDetail(@RequestParam (required = false) int storeIdx) throws BaseException{

        // 없을 경우 어떻게 처리?
        //        if (storeId==null){
//            return new BaseResponse<>(EMPTY_STOREIDX_PARAM);
//        }
        GetStoreDetailRes getStoreDetailRes = storeProvider.getStoreDetail(storeIdx);
        return new BaseResponse<>(getStoreDetailRes);
    }

}
