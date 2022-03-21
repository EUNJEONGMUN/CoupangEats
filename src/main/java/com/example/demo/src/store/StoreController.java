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

import static com.example.demo.config.BaseResponseStatus.*;

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
    public BaseResponse<GetStoreDetailRes> getStoreDetail(@RequestParam(required = false) String storeIdx) throws BaseException{

        // int는 null 값이 될 수 없기 때문에 String으로 받는다.
        // storeIdx
        if (storeIdx == null){
            return new BaseResponse<>(EMPTY_STOREIDX_PARAM);
        }
        if (storeProvider.checkStore(Integer.parseInt(storeIdx))==0){
            return new BaseResponse<>(EMPTY_STORE);
        }

        GetStoreDetailRes getStoreDetailRes = storeProvider.getStoreDetail(Integer.parseInt(storeIdx));
        return new BaseResponse<>(getStoreDetailRes);
    }

}
