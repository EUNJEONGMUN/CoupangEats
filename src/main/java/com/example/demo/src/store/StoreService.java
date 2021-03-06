package com.example.demo.src.store;

import com.example.demo.config.BaseException;
import com.example.demo.src.store.model.Req.PostReviewUrlReq;
import com.example.demo.src.store.model.Req.PutReviewReq;
import com.example.demo.src.store.model.Req.PostReviewReq;
import com.example.demo.src.store.model.Req.PutReviewUrlReq;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

@Service
public class StoreService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final StoreDao storeDao;
    private final StoreProvider storeProvider;
    private final JwtService jwtService;
    private final int FAIL = 0;

    @Autowired
    public StoreService(StoreDao storeDao, StoreProvider storeProvider, JwtService jwtService){
        this.storeDao = storeDao;
        this.jwtService = jwtService;
        this.storeProvider = storeProvider;

    }

    /**
     * 즐겨찾기 등록 API
     * [POST] /stores/favorite?storeIdx=
     * /favorite?storeIdx=
     * @return BaseResponse<String>
     */
    public void createFavoriteStore(int userIdx, int storeIdx) throws BaseException {
        try {
            int result = storeDao.createFavoriteStore(userIdx, storeIdx);
            if (result == FAIL){
                throw new BaseException(FAIL_POST_FAVORITE_STORE);
            }
        } catch (Exception exception) {
            System.out.println("createAddress"+exception);
            throw new BaseException(DATABASE_ERROR);
        }

    }

    /**
     * 즐겨찾기 해제 API
     * [PUT] /stores/favorite?storeIdx=
     * /favorite?storeIdx=
     * @return BaseResponse<String>
     */
    public void deleteFavoriteStore(int userIdx, String[] storeIdx) throws BaseException {
        try {
            int result = storeDao.deleteFavoriteStore(userIdx, storeIdx);
            if (result == FAIL){
                throw new BaseException(FAIL_PUT_FAVORITE_STORE);
            }
        } catch (Exception exception) {
            System.out.println("deleteFavoriteStore"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * 리뷰 작성 API
     * [POST] /stores/review/new?userOrderIdx=
     * /new?userOrderIdx=
     * @return BaseResponse<String>
     */
    public void createReview(int userIdx, int userOrderIdx, PostReviewReq postReviewReq, List<String> imageList) throws BaseException {
        try {
            int result = storeDao.createReview(userIdx, userOrderIdx, postReviewReq, imageList);
            if (result == FAIL){
                throw new BaseException(FAIL_POST_REVIEW);
            }
        } catch (Exception exception) {
            System.out.println("deleteFavoriteStore"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }





    /**
     * 리뷰 수정 API
     * [PUT] /stores/review?reviewIdx=
     * @return BaseResponse<String>
     */
    public void modifyReview(int userIdx, int reviewIdx, PutReviewReq putReviewReq, List<String> imageList) throws BaseException {
        try {
            int result = storeDao.modifyReview(userIdx, reviewIdx, putReviewReq, imageList);
            if (result == FAIL){
                throw new BaseException(FAIL_MODIFY_REVIEW);
            }
        } catch (Exception exception) {
            System.out.println("deleteFavoriteStore"+exception);
            throw new BaseException(DATABASE_ERROR);
        }

    }


    /**
     * 리뷰 삭제 API
     * [PATCH] /stores/review
     * @return BaseResponse<String>
     */
    public void deleteReview(int userOrderIdx) throws BaseException {
        try {
            int result = storeDao.deleteReview(userOrderIdx);
            if (result == FAIL){
                throw new BaseException(FAIL_DELETE_REVIEW);
            }
        } catch (Exception exception) {
            System.out.println("deleteReview"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 리뷰 도움이 돼요 등록 API - 아무 것도 선택 안했을 때
     * [POST] /stores/review/liked?reviewIdx
     * /liked?reviewIdx
     * @return BaseResponse<String>
     */
    public void createReviewLiked(int userIdx, int reviewIdx, String isHelped) throws BaseException {
        try {
            int result = storeDao.createReviewLiked(userIdx, reviewIdx, isHelped);
            if (result == FAIL){
                throw new BaseException(FAIL_LIKED_REVIEW);
            }
        } catch (Exception exception) {
            System.out.println("createReviewLiked"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 리뷰 도움이 돼요 등록 API - toggle
     * [POST] /stores/review/liked?reviewIdx
     * /liked?reviewIdx
     * @return BaseResponse<String>
     */
    public void createReviewLikedToggle(int userIdx, int reviewIdx, String isHelped) throws BaseException{
        try {
            int deleteSuccess = storeDao.deleteExistsReviewLiked(userIdx, reviewIdx);
            if (deleteSuccess == FAIL){
                throw new BaseException(FAIL_DELETE_EXISTS_LIKED_REVIEW);
            }
            int result = storeDao.createReviewLiked(userIdx, reviewIdx, isHelped);
            if (result == FAIL){
                throw new BaseException(FAIL_LIKED_REVIEW);
            }
        } catch (Exception exception) {
            System.out.println("createReviewLikedToggle"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }
    /**
     * 리뷰 도움이 돼요 삭제 API
     * [PATCH] /stores/review/liked/deletion
     * @return BaseResponse<String>
     */
    public void deleteReviewLiked(int userIdx, int reviewIdx) throws BaseException {
        try {
            int deleteSuccess = storeDao.deleteExistsReviewLiked(userIdx, reviewIdx);
            if (deleteSuccess == FAIL){
                throw new BaseException(FAIL_DELETE_EXISTS_LIKED_REVIEW);
            }
        } catch (Exception exception) {
            System.out.println("deleteReviewLiked"+exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
