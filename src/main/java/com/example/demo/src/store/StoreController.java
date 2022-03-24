package com.example.demo.src.store;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.store.model.Res.*;
import com.example.demo.src.store.model.StoreHome;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

@RestController
@CrossOrigin(origins = "http://localhost:9009")
@RequestMapping("/stores")
public class StoreController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final StoreProvider storeProvider;
    @Autowired
    private final StoreService storeService;
    @Autowired
    private final JwtService jwtService;

    public StoreController(StoreProvider storeProvider, StoreService storeService, JwtService jwtService){
        this.storeProvider = storeProvider;
        this.storeService = storeService;
        this.jwtService = jwtService;
    }

    /**
     * 홈 화면 조회 API
     * [GET] /stores/home
     * /home?sort=&isCheetah&fee=&minimum=&isTogo=&isCoupon&categoryIdx=
     * @return BaseResponse<List<GetStoreHomeRes>>
     */
//    @UnAuth
    @ResponseBody
    @GetMapping("/home")
    public BaseResponse<List<GetStoreHomeRes>> getStoreHome(StoreHome storeHome) throws BaseException{
        if (storeHome.getCategoryIdx()!=0 && storeProvider.checkStoreCategory(storeHome.getCategoryIdx())==0){
            return new BaseResponse<>(EMPTY_STORE_CATEGORY);
        }
        List<GetStoreHomeRes> getStoreHomeRes = storeProvider.getStoreHome(storeHome);
        return new BaseResponse<>(getStoreHomeRes);
    }

    /**
     * 가게 상세 화면 조회 조회 API
     * [GET] /stores/detail?storeIdx=
     * @return BaseResponse<GetStoreDetailRes>
     */
//    @UnAuth
    @ResponseBody
    @GetMapping("/detail")
    public BaseResponse<GetStoreDetailRes> getStoreDetail(@RequestParam(required = false, defaultValue = "0") int storeIdx) throws BaseException{

        // storeIdx
        if (storeIdx == 0){
            return new BaseResponse<>(EMPTY_STOREIDX_PARAM);
        }
        if (storeProvider.checkStore(storeIdx)==0){
            return new BaseResponse<>(EMPTY_STORE);
        }


        GetStoreDetailRes getStoreDetailRes = storeProvider.getStoreDetail(storeIdx);
        return new BaseResponse<>(getStoreDetailRes);
    }

    /**
     * 메뉴 상세 화면 조회 조회 API
     * [GET] /stores/options?storeIdx=&menuIdx=
     * @return BaseResponse<GetStoreMenuOptionsRes>
     */
//    @UnAuth
    @ResponseBody
    @GetMapping("/options")
    public BaseResponse<GetStoreMenuOptionsRes> getMenuOptions(@RequestParam(required = false, defaultValue = "0") int storeIdx,
                                                               @RequestParam(required = false, defaultValue = "0") int menuIdx) throws BaseException{

        if (storeIdx == 0){ // storeIdx가 없을 경우
            return new BaseResponse<>(EMPTY_STOREIDX_PARAM);
        }
        if (menuIdx == 0){ // menuIdx가 없을 경우
            return new BaseResponse<>(EMPTY_MENUIDX_PARAM);
        }

        if (storeProvider.checkStore(storeIdx)==0){ // 가게가 없을 경우
            return new BaseResponse<>(EMPTY_STORE);
        }
        if (storeProvider.checkMenu(menuIdx)==0){ // 메뉴가 없을 경우
            return new BaseResponse<>(EMPTY_MENU);
        }
        if (storeProvider.checkMenuOwner(menuIdx)!=storeIdx){ // 메뉴가 속한 가게가 storeIdx와 일치하지 않을 경우
            return new BaseResponse<>(INCONSISTENCY_STORE_OWNER);
        }

        GetStoreMenuOptionsRes getStoreMenuOptionsRes = storeProvider.getMenuOptions(menuIdx);
        return new BaseResponse<>(getStoreMenuOptionsRes);



    }
}
