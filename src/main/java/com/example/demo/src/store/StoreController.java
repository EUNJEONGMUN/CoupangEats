package com.example.demo.src.store;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.store.model.Res.*;
import com.example.demo.src.store.model.StoreHome;
import com.example.demo.src.user.UserProvider;
import com.example.demo.src.user.UserService;
import com.example.demo.src.user.model.UserLocation;
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
    @Autowired
    private final UserProvider userProvider;

    public StoreController(StoreProvider storeProvider, StoreService storeService, JwtService jwtService,UserProvider userProvider){
        this.storeProvider = storeProvider;
        this.storeService = storeService;
        this.jwtService = jwtService;
        this.userProvider = userProvider;
    }

    /**
     * 홈 화면 조회 API
     * [GET] /stores/home
     * /home?&longitude=&latitude=&categoryIdx=
     * @return BaseResponse<List<GetStoreHomeRes>>
     */
    @ResponseBody
    @GetMapping("/home")
    public BaseResponse<List<GetStoreHomeRes>> getStoreHome(@RequestParam(required = false, defaultValue = "0") double longitude,
                                                            @RequestParam(required = false, defaultValue = "0") double latitude,
                                                            @RequestParam(required = false, defaultValue = "0") int categoryIdx) throws BaseException{

        int userIdx= jwtService.getUserIdxOption();
        if (latitude==0 || longitude==0){
            return new BaseResponse<>(EMPTY_POSITION_PARAM);
        }

        UserLocation userLocation = new UserLocation();

        if (userIdx == 0){
            userLocation.setUserLongitude(longitude);
            userLocation.setUserLatitude(latitude);
        } else{
            userLocation = storeProvider.getNowUserLocation(userIdx);
            if (userLocation.getUserLatitude()==0 || userLocation.getUserLatitude()==0){
                userLocation.setUserLongitude(longitude);
                userLocation.setUserLatitude(latitude);
            }
        }


        List<GetStoreHomeRes> getStoreHomeRes = storeProvider.getStoreHome(userLocation, categoryIdx);
        return new BaseResponse<>(getStoreHomeRes);
    }

    /**
     * 가게 상세 화면 조회 조회 API
     * [GET] /stores/detail?storeIdx=
     * @return BaseResponse<GetStoreDetailRes>
     */
    @ResponseBody
    @GetMapping("/detail")
    public BaseResponse<GetStoreDetailRes> getStoreDetail(@RequestParam(required = false, defaultValue = "0") double longitude,
                                                          @RequestParam(required = false, defaultValue = "0") double latitude,
                                                          @RequestParam(required = false, defaultValue = "0") int storeIdx) throws BaseException{
        int userIdx= jwtService.getUserIdxOption();
        if (latitude==0 || longitude==0){
            return new BaseResponse<>(EMPTY_POSITION_PARAM);
        }

        UserLocation userLocation = new UserLocation();

        if (userIdx == 0){
            userLocation.setUserLongitude(longitude);
            userLocation.setUserLatitude(latitude);
        } else{
            userLocation = storeProvider.getNowUserLocation(userIdx);
            if (userLocation.getUserLatitude()==0 || userLocation.getUserLatitude()==0){
                userLocation.setUserLongitude(longitude);
                userLocation.setUserLatitude(latitude);
            }
        }

        // storeIdx
        if (storeIdx == 0){
            return new BaseResponse<>(EMPTY_STOREIDX_PARAM);
        }
        if (storeProvider.checkStore(storeIdx)==0){
            return new BaseResponse<>(EMPTY_STORE);
        }


        GetStoreDetailRes getStoreDetailRes = storeProvider.getStoreDetail(userLocation, storeIdx, userIdx);
        return new BaseResponse<>(getStoreDetailRes);
    }

    /**
     * 메뉴 상세 화면 조회 API
     * [GET] /stores/options?storeIdx=&menuIdx=
     * @return BaseResponse<GetStoreMenuOptionsRes>
     */
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

    /**
     * 가게별 리뷰 조회 API
     * [GET] /stores/review-list
     * /review-list?storeIdx=
     * @return BaseResponse<List<GetStoreReviewListRes>>
     */
    @ResponseBody
    @GetMapping("/review-list")
    public BaseResponse<List<GetStoreReviewListRes>> getStoreReviews(@RequestParam(required = false, defaultValue = "0") int storeIdx) throws BaseException {

        int userIdx= jwtService.getUserIdxOption();
        if (storeIdx ==0){
            return new BaseResponse<>(EMPTY_STOREIDX_PARAM);
        }
        List<GetStoreReviewListRes> getStoreReviewListRes = storeProvider.getStoreReviews(userIdx, storeIdx);
        return new BaseResponse<>(getStoreReviewListRes);

    }

//    /**
//     * 작성한 리뷰 조회 API
//     * [GET] /stores/review
//     * /review?storeIdx=&reviewIdx
//     * @return BaseResponse<GetFavoriteListRes>
//     */
//    public BaseResponse<GetStoreReviewListRes> getStoreMyReviews(@RequestParam(required = false, defaultValue = "0") int storeIdx) throws BaseException {
//
//    }

    /**
     * 즐겨찾기 등록 API
     * [POST] /stores/favorite?storeIdx=
     * /favorite?storeIdx=
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/favorite")
    public BaseResponse<String> createFavoriteStore(@RequestParam(required = false) int storeIdx) throws BaseException {

        int userIdx= jwtService.getUserIdx();
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        if (storeIdx == 0){ // storeIdx가 없을 경우
            return new BaseResponse<>(EMPTY_STOREIDX_PARAM);
        }
        // 이미 좋아요 한 가게가 있는지 확인
        if (storeProvider.checkFavoriteStore(userIdx, storeIdx) !=0){
            return new BaseResponse<>(FAVORITE_STORE_ALREADY);
        }

        storeService.createFavoriteStore(userIdx, storeIdx);
        String result = "";
        return new BaseResponse<>(result);
    }

    /**
     * 즐겨찾기 해제 API
     * [PUT] /stores/favorite?storeIdx=
     * /favorite?storeIdx=
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PutMapping("/favorite")
    public BaseResponse<String> deleteFavoriteStore(@RequestParam(required = false) String[] storeIdx) throws BaseException {
        int userIdx= jwtService.getUserIdx();
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }
        if (storeIdx.length == 0){ // storeIdx가 없을 경우
            return new BaseResponse<>(EMPTY_STOREIDX_PARAM);
        }

        // 이미 좋아요 한 가게가 있는지 확인 - 1개만 들어올 때
        if (storeProvider.checkFavoriteStore(userIdx, Integer.parseInt(storeIdx[0])) ==0){
            return new BaseResponse<>(FAVORITE_STORE_NOT_ALREADY);
        }

        storeService.deleteFavoriteStore(userIdx, storeIdx);
        String result = "";
        return new BaseResponse<>(result);
    }

    /**
     * 즐겨찾기 조회 API
     * [GET] /stores/favorite-list
     * @return BaseResponse<List<GetFavoriteListRes>>
     */
    @ResponseBody
    @GetMapping("/favorite-list")
    public BaseResponse<List<GetFavoriteListRes>> getFavoriteList(@RequestParam(required = false, defaultValue = "0") double longitude,
                                                                  @RequestParam(required = false, defaultValue = "0") double latitude) throws BaseException{
        int userIdx= jwtService.getUserIdx();
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        if (latitude==0 || longitude==0){
            return new BaseResponse<>(EMPTY_POSITION_PARAM);
        }

        UserLocation userLocation = storeProvider.getNowUserLocation(userIdx);
        if (userLocation.getUserLatitude()==0 || userLocation.getUserLatitude()==0){
            userLocation.setUserLongitude(longitude);
            userLocation.setUserLatitude(latitude);
        }

        List<GetFavoriteListRes> getFavoriteListRes = storeProvider.getFavoriteList(userIdx, userLocation);
        return new BaseResponse<>(getFavoriteListRes);

    }


}
