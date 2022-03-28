package com.example.demo.src.store;

import com.example.demo.config.BaseException;
import com.example.demo.src.store.model.Res.*;
import com.example.demo.src.store.model.StoreReviewIdx;
import com.example.demo.src.user.model.UserLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public List<GetStoreHomeRes> getStoreHome(UserLocation userLocation, int categoryIdx) throws BaseException {
        try {

            List<Integer> StoreList = storeDao.findStoreIdxList(categoryIdx);
            System.out.println("here");
            List<GetStoreHomeRes> getStoreHomeRes = new ArrayList<>();
            System.out.println("here");
            for(int idx:StoreList){
                System.out.println(">>idx>>"+idx);
                GetStoreHomeRes storeHome = storeDao.getStoreHome(idx, userLocation);
                System.out.println("here");
                getStoreHomeRes.add(storeHome);
                System.out.println("here");
            }
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
    public GetStoreDetailRes getStoreDetail(UserLocation userLocation, int storeIdx, int userIdx) throws BaseException {
        try {
            GetStoreDetailRes getStoreDetailRes = storeDao.getStoreDetail(userLocation, storeIdx, userIdx);
            return getStoreDetailRes;
        } catch (Exception exception) {
            System.out.println("store_detail-> "+ exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 메뉴 상세 화면 조회 조회 API
     * [GET] /stores/detail?storeIdx=&menuIdx=
     * @return BaseResponse<GetStoreMenuOptionsRes>
     */
    public GetStoreMenuOptionsRes getMenuOptions(int menuIdx) throws BaseException {
        try {
            GetStoreMenuOptionsRes getStoreMenuOptionsRes = storeDao.getMenuOptions(menuIdx);
            return getStoreMenuOptionsRes;
        } catch (Exception exception) {
            System.out.println("store_menu_options-> "+ exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 즐겨찾기 조회 API
     * [GET] /stores/favorite-list
     * @return BaseResponse<List<GetFavoriteListRes>>
     */
    public List<GetFavoriteListRes> getFavoriteList(int userIdx, UserLocation userLocation) throws BaseException{
        try {

            List<Integer> storeList = storeDao.getFavoriteStoreIdx(userIdx);

            List<GetFavoriteListRes> getFavoriteListRes = new ArrayList<>();
            for (int idx: storeList){
                GetFavoriteListRes favoriteStore = storeDao.getFavoriteList(userIdx, idx, userLocation);
                getFavoriteListRes.add(favoriteStore);
            }

            return getFavoriteListRes;
        } catch (Exception exception) {
            System.out.println("getFavoriteList"+exception);
            throw new BaseException(DATABASE_ERROR);
        }

    }

    /**
     * 가게별 리뷰 조회 API
     * [GET] /stores/review-list
     * /review-list?storeIdx=
     * @return BaseResponse<List<GetStoreReviewListRes>>
     */
    public List<GetStoreReviewListRes> getStoreReviews(int userIdx, int storeIdx) throws BaseException{
        try {
            List<StoreReviewIdx> reviewList = storeDao.getStoreReviewIdx(storeIdx);

            List<GetStoreReviewListRes> getStoreReviewListRes = new ArrayList<>();
            for (StoreReviewIdx idx:reviewList){
                GetStoreReviewListRes storeReviewList = storeDao.getStoreReviews(userIdx, storeIdx, idx);
                getStoreReviewListRes.add(storeReviewList);
            }
            return getStoreReviewListRes;
        } catch (Exception exception) {
            System.out.println("getStoreReviews"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 작성한 리뷰 조회 API
     * [GET] /stores/review?userOrderIdx=
     * /review?userOrderIdx=
     * @return BaseResponse<GetStoreReviewListRes>
     */
    public GetStoreMyReviewRes getStoreMyReview(int userIdx, int userOrderIdx) throws BaseException {
        try {
            GetStoreMyReviewRes getStoreMyReviewRes = storeDao.getStoreMyReview(userIdx, userOrderIdx);
            return getStoreMyReviewRes;
        } catch (Exception exception) {
            System.out.println("getStoreMyReview"+exception);
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

    // 메뉴 존재 여부 확인
    public int checkMenu(int menuIdx) throws BaseException {
        try {
            return storeDao.checkMenu(menuIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 메뉴가 속한 가게 아이디 확인
    public int checkMenuOwner(int menuIdx) throws BaseException {
        try {
            return storeDao.checkMenuOwner(menuIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 가게 카테고리 존재 여부 확인
    public int checkStoreCategory(int categoryIdx) throws BaseException {
        try {
            return storeDao.checkStoreCategory(categoryIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 사용자의 현재 위치 찾기
    public UserLocation getNowUserLocation(int userIdx) throws BaseException {
        try {
            return storeDao.getNowUserLocation(userIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 즐겨찾기 한 가게 찾기
    public int checkFavoriteStore(int userIdx, int storeIdx) throws BaseException {
        try {
            return storeDao.checkFavoriteStore(userIdx,storeIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 주문 사용자 확인
    public int checkUserOrderOwner(int userIdx, int userOrderIdx) throws BaseException {
        try {
            return storeDao.checkUserOrderOwner(userIdx,userOrderIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 리뷰 존재 확인
    public int checkUserReview(int userIdx, int userOrderIdx) throws BaseException  {
        try {
            return storeDao.checkUserReview(userIdx,userOrderIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
