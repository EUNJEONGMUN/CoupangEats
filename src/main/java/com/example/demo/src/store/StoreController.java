package com.example.demo.src.store;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.S3Image.S3Uploader;
import com.example.demo.src.orders.OrderProvider;
import com.example.demo.src.store.model.Req.*;
import com.example.demo.src.store.model.Res.*;
import com.example.demo.src.user.UserProvider;
import com.example.demo.src.user.model.UserLocation;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
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
    @Autowired
    private final OrderProvider orderProvider;
    @Autowired
    private final S3Uploader s3Uploader;


    public StoreController(StoreProvider storeProvider, StoreService storeService, JwtService jwtService,UserProvider userProvider, OrderProvider orderProvider, S3Uploader s3Uploader){
        this.storeProvider = storeProvider;
        this.storeService = storeService;
        this.jwtService = jwtService;
        this.userProvider = userProvider;
        this.orderProvider = orderProvider;
        this.s3Uploader = s3Uploader;
    }

    /**
     * 홈 화면 조회 API
     * [GET] /stores/home
     * /home?&longitude=&latitude=&categoryIdx=&sort=&isCheetah&deliveryFee=&minimumPrice&isToGo=&isCoupon=
     * @return BaseResponse<List<GetStoreHomeRes>>
     */
    @ResponseBody
    @GetMapping("/home")
    public BaseResponse<GetStoreHomeTotalRes> getStoreHome(@RequestParam(required = false, defaultValue = "0") double longitude,
                                                            @RequestParam(required = false, defaultValue = "0") double latitude,
                                                            @RequestParam(required = false, defaultValue = "0") int categoryIdx,
                                                            @RequestParam(required = false, defaultValue = "order") String sort,
                                                            @RequestParam(required = false, defaultValue = "N") String isCheetah,
                                                            @RequestParam(required = false, defaultValue = "전체") String deliveryFee,
                                                            @RequestParam(required = false, defaultValue = "전체") String minimumPrice,
                                                            @RequestParam(required = false, defaultValue = "N") String isToGo,
                                                            @RequestParam(required = false, defaultValue = "N") String isCoupon) throws BaseException{

        // 헤더에 사용자 idx가 있다면 추출
        int userIdx= jwtService.getUserIdxOption();

        // 위치 정보가 없는지 확인
        if (latitude==0 || longitude==0){
            return new BaseResponse<>(EMPTY_POSITION_PARAM);
        }


        UserLocation userLocation = new UserLocation();

        if (userIdx == 0){ // 사용자 idx가 없을 때 userLocation을 파라미터값으로 설정
            userLocation.setUserLongitude(longitude);
            userLocation.setUserLatitude(latitude);
        } else{// 사용자 idx가 있을 때

            // 사용자의 현재 위치 가져오기
            userLocation = storeProvider.getNowUserLocation(userIdx);

            // 사용자가 현재 주소로 설정한 값이 없을 때
            if (userLocation.getUserLatitude()==0 || userLocation.getUserLatitude()==0){
                // userLocation을 파라미터값으로 설정
                userLocation.setUserLongitude(longitude);
                userLocation.setUserLatitude(latitude);
            }
        }

        if (!(deliveryFee.equals("전체") || deliveryFee.equals("무료배달") || deliveryFee.equals("1000")|| deliveryFee.equals("2000")|| deliveryFee.equals("3000"))){
            return new BaseResponse<>(INVALID_DELIVERY_FEE_PARAM);
        }
        if (!(minimumPrice.equals("전체") || minimumPrice.equals("5000") ||minimumPrice.equals("10000") ||minimumPrice.equals("12000") ||minimumPrice.equals("15000"))){
            return new BaseResponse<>(INVALID_MINIMUM_PRICE_PARAM);
        }

        GetStoreHomeReq getStoreHomeReq = new GetStoreHomeReq(sort, isCheetah, deliveryFee, minimumPrice, isToGo, isCoupon);

        List<GetStoreHomeRes> getStoreHomeRes = storeProvider.getStoreHome(categoryIdx, userLocation, getStoreHomeReq, "default");
        List<GetStoreHomeRes> getOnlyEatsStore = storeProvider.getStoreHome(categoryIdx, userLocation, getStoreHomeReq, "onlyEats");
        List<GetStoreHomeRes> getFranchiseStore = storeProvider.getStoreHome(categoryIdx, userLocation, getStoreHomeReq, "franchise");
        List<GetStoreHomeRes> getNewStore = storeProvider.getStoreHome(categoryIdx, userLocation, getStoreHomeReq, "new");
        GetStoreHomeTotalRes getStoreHomeTotalRes = new GetStoreHomeTotalRes(getOnlyEatsStore, getFranchiseStore, getNewStore, getStoreHomeRes);
        return new BaseResponse<>(getStoreHomeTotalRes);
    }

    /**
     * 타입별 홈화면 조회 API
     * [GET] /stores/home/type
     * /type?&longitude=&latitude=&categoryIdx=&sort=&isCheetah&deliveryFee=&minimumPrice&isToGo=&isCoupon=&type=
     * @return BaseResponse<List<GetStoreHomeRes>>
     */
    @ResponseBody
    @GetMapping("/home/type")
    public BaseResponse<List<GetStoreHomeRes>> getTypeHome(@RequestParam(required = false, defaultValue = "0") double longitude,
                                                           @RequestParam(required = false, defaultValue = "0") double latitude,
                                                           @RequestParam(required = false, defaultValue = "order") String sort,
                                                           @RequestParam(required = false, defaultValue = "N") String isCheetah,
                                                           @RequestParam(required = false, defaultValue = "전체") String deliveryFee,
                                                           @RequestParam(required = false, defaultValue = "전체") String minimumPrice,
                                                           @RequestParam(required = false, defaultValue = "N") String isToGo,
                                                           @RequestParam(required = false, defaultValue = "N") String isCoupon,
                                                                    @RequestParam(required = false, defaultValue = "default") String type) throws BaseException{

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

        if (!(deliveryFee.equals("전체") || deliveryFee.equals("무료배달") || deliveryFee.equals("1000")|| deliveryFee.equals("2000")|| deliveryFee.equals("3000"))){
            return new BaseResponse<>(INVALID_DELIVERY_FEE_PARAM);
        }
        if (!(minimumPrice.equals("전체") || minimumPrice.equals("5000") ||minimumPrice.equals("10000") ||minimumPrice.equals("12000") ||minimumPrice.equals("15000"))){
            return new BaseResponse<>(INVALID_MINIMUM_PRICE_PARAM);
        }

        if (!(type.equals("onlyEats") || type.equals("franchise") || type.equals("recent"))){
            return new BaseResponse<>(INVALID_TYPE_PARAM);
        }

        GetStoreHomeReq getStoreHomeReq = new GetStoreHomeReq(sort, isCheetah, deliveryFee, minimumPrice, isToGo, isCoupon);

        List<GetStoreHomeRes> getTypeHomeRes = storeProvider.getTypeStoreHome(userLocation, getStoreHomeReq, type);

        return new BaseResponse<>(getTypeHomeRes);
    }



    /**
     * 가게 상세 화면 조회 API
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
     * /review-list?storeIdx=&sort=
     * @return BaseResponse<List<GetStoreReviewListRes>>
     */
    @ResponseBody
    @GetMapping("/review-list")
    public BaseResponse<List<GetStoreReviewListRes>> getStoreReviews(@RequestParam(required = false, defaultValue = "0") int storeIdx,
                                                                     @RequestParam(required = false, defaultValue = "recent") String sort,
                                                                     @RequestParam(required = false, defaultValue = "Y") String isPhoto) throws BaseException {

        int userIdx= jwtService.getUserIdxOption();
        if (storeIdx ==0){
            return new BaseResponse<>(EMPTY_STOREIDX_PARAM);
        }
        List<GetStoreReviewListRes> getStoreReviewListRes = storeProvider.getStoreReviews(userIdx, storeIdx, sort, isPhoto);
        return new BaseResponse<>(getStoreReviewListRes);

    }

    /**
     * 작성한 리뷰 조회 API
     * [GET] /stores/review?userOrderIdx=
     * /review?userOrderIdx=
     * @return BaseResponse<GetStoreReviewListRes>
     */
    @ResponseBody
    @GetMapping("/review")
    public BaseResponse<GetStoreMyReviewRes> getStoreMyReview(@RequestParam(required = false, defaultValue = "0") int userOrderIdx) throws BaseException {

        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        if (userOrderIdx ==0){
            return new BaseResponse<>(EMPTY_USER_ORDER_IDX_PARAM);
        }

        // userOrderIdx가 사용자의 idx인지 확인
        if (orderProvider.checkOrderOwner(userIdx, userOrderIdx) == 0){
            return new BaseResponse<>(USER_ORDER_NOT_EXISTS);
        }
        if (storeProvider.checkUserReview(userIdx, userOrderIdx) == 0){
            return new BaseResponse<>(REVIEW_NOT_EXISTS);
        }

        GetStoreMyReviewRes getStoreMyReviewRes = storeProvider.getStoreMyReview(userIdx, userOrderIdx);
        return new BaseResponse<>(getStoreMyReviewRes);

    }


    /**
     * 리뷰 작성 API
     * [POST] /stores/review/new
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/review/new")
    public BaseResponse<String> createReview(PostReviewReq postReviewReq,
                                             @RequestParam(value = "image", required = false) List<MultipartFile> multipartFile) throws BaseException, IOException {

        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        if (postReviewReq.getUserOrderIdx() ==0){
            return new BaseResponse<>(EMPTY_USER_ORDER_IDX_PARAM);
        }
        // 주문 존재 여부 확인
        if (orderProvider.checkOrder(postReviewReq.getUserOrderIdx())==0){
            return new BaseResponse<>(USER_ORDER_NOT_EXISTS);
        }

        // 주문 소유자 확인
        if (orderProvider.checkOrderOwner(userIdx, postReviewReq.getUserOrderIdx())==0){
            return new BaseResponse<>(INCONSISTENCY_ORDER_USER);
        }

        // 리뷰 작성여부 확인
        //
        if (storeProvider.checkUserReview(userIdx, postReviewReq.getUserOrderIdx()) != 0){
            return new BaseResponse<>(REVIEW_ALREADY_EXISTS);
        }

        // 리뷰 작성 기한 확인
        if (!storeProvider.checkOrderTime(postReviewReq.getUserOrderIdx())){
            return new BaseResponse<>(EXPIRATION_OR_REVIEW);
        }

        List<String> imageList = new ArrayList<>();
        if (multipartFile!=null && multipartFile.size()!=0){
            for (MultipartFile file:multipartFile){
                String imageUrl = s3Uploader.upload(file, "static");
                imageList.add(imageUrl);
            }
        }


        storeService.createReview(userIdx, postReviewReq.getUserOrderIdx(), postReviewReq, imageList);
        String result = "";
        return new BaseResponse<>(result);


    }







    /**
     * 리뷰 수정 API
     * [PUT] /stores/review
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PutMapping("/review")
    public BaseResponse<String> modifyReview(PutReviewReq putReviewReq,
                                             @RequestParam(value = "image", required = false) List<MultipartFile> multipartFile) throws BaseException, IOException {
        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }
        // 리뷰 아이디 찾기
        int reviewIdx = storeProvider.findReviewIdx(putReviewReq.getUserOrderIdx());
        if (reviewIdx==0){
            return new BaseResponse<>(REVIEW_NOT_EXISTS);
        }

        // 리뷰 작성자 확인
        if (storeProvider.checkReviewOwner(userIdx, reviewIdx)==0){
            return new BaseResponse<>(INCONSISTENCY_REVIEW_USER);
        }

        // 리뷰 수정 가능 기간 확인
        if (!storeProvider.checkReviewUploadTime(reviewIdx)){
            return new BaseResponse<>(EXPIRATION_OF_REVIEW_EDIT);
        }



        List<String> imageList = new ArrayList<>();
        if (multipartFile!=null && multipartFile.size()!=0){
            for (MultipartFile file:multipartFile){
                String imageUrl = s3Uploader.upload(file, "static");
                imageList.add(imageUrl);
            }
        }

        storeService.modifyReview(userIdx, reviewIdx, putReviewReq, imageList);
        String result = "";
        return new BaseResponse<>(result);


    }



    /**
     * 리뷰 삭제 API
     * [PATCH] /stores/review
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/review/deletion")
    public BaseResponse<String> deleteReview(@Valid @RequestBody PatchReviewReq patchReviewReq) throws BaseException {
        int userIdx= jwtService.getUserIdx();

        // 사용자 존재 여부 확인
        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }
        // 리뷰 아이디 찾기
        int reviewIdx = storeProvider.findReviewIdx(patchReviewReq.getUserOrderIdx());
        if (reviewIdx==0){
            return new BaseResponse<>(REVIEW_NOT_EXISTS);
        }

        // 리뷰 작성자 확인
        if (storeProvider.checkReviewOwner(userIdx, reviewIdx)==0){
            return new BaseResponse<>(INCONSISTENCY_REVIEW_USER);
        }

        storeService.deleteReview(patchReviewReq.getUserOrderIdx());
        String result = "";
        return new BaseResponse<>(result);
    }



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
    public BaseResponse<GetFavoriteListRes> getFavoriteList(@RequestParam(required = false, defaultValue = "0") double longitude,
                                                                  @RequestParam(required = false, defaultValue = "0") double latitude,
                                                                  @RequestParam(required = false, defaultValue = "frequent") String sort) throws BaseException{
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

        if (!(sort.equals("frequent") || sort.equals("recentOrder") || sort.equals("recentAdd"))){
            return new BaseResponse<>(INVALID_STATUS);
        }
        GetFavoriteListRes getFavoriteListRes = storeProvider.getFavoriteList(userIdx, userLocation, sort);
        return new BaseResponse<>(getFavoriteListRes);

    }

    /**
     * 리뷰 도움이 돼요 등록 API
     * [POST] /stores/review/liked
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/review/liked")
    public BaseResponse<String> createReviewLiked(@Valid @RequestBody PostLikedReviewReq postLikedReviewReq) throws BaseException {

        int userIdx= jwtService.getUserIdx();

        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }


        // 리뷰가 존재하는지 확인
        if (storeProvider.checkReviewExists(postLikedReviewReq.getReviewIdx())==0){
            return new BaseResponse<>(EMPTY_REVIEWIDX);
        }

        if (!(postLikedReviewReq.getIsHelped().equals("G")||postLikedReviewReq.getIsHelped().equals("B"))){
            return new BaseResponse<>(INVALID_STATUS);
        }
        // 이미 리뷰 한 글인지 확인
        // 리뷰 한 기록이 없을 경우 "N"
        // 있을 경우 "G" or "B"
        String dbIsHelped = storeProvider.checkLikedReview(userIdx, postLikedReviewReq.getReviewIdx());
        if (dbIsHelped.equals(postLikedReviewReq.getIsHelped())){
            return new BaseResponse<>(LIKED_REVIEW_ALREADY);
        }

        if (dbIsHelped.equals("N")){
            storeService.createReviewLiked(userIdx, postLikedReviewReq.getReviewIdx(), postLikedReviewReq.getIsHelped());
        }

        storeService.createReviewLikedToggle(userIdx, postLikedReviewReq.getReviewIdx(), postLikedReviewReq.getIsHelped());

        String result = "";
        return new BaseResponse<>(result);

    }

    /**
     * 리뷰 도움이 돼요 삭제 API
     * [PATCH] /stores/review/liked/deletion
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/review/liked/deletion")
    public BaseResponse<String> deleteReviewLiked(@Valid @RequestBody PatchLikedReviewReq patchLikedReviewReq) throws BaseException {
        int userIdx= jwtService.getUserIdx();

        if (userProvider.checkUser(userIdx)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        // 리뷰가 존재하는지 확인
        if (storeProvider.checkReviewExists(patchLikedReviewReq.getReviewIdx())==0){
            return new BaseResponse<>(EMPTY_REVIEWIDX);
        }
        String dbIsHelped = storeProvider.checkLikedReview(userIdx, patchLikedReviewReq.getReviewIdx());

        if (dbIsHelped.equals("N")){
            return new BaseResponse<>(EMPTY_LIKED_REVIEW);
        }
        storeService.deleteReviewLiked(userIdx, patchLikedReviewReq.getReviewIdx());
        String result = "";
        return new BaseResponse<>(result);
    }


















    @PostMapping("/images")
    public String upload(@RequestParam("data") MultipartFile multipartFile) throws IOException {
        s3Uploader.upload(multipartFile, "static");
        return "test";
    }
}
