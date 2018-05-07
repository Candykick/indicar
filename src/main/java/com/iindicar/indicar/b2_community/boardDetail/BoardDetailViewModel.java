package com.iindicar.indicar.b2_community.boardDetail;

import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.iindicar.indicar.BaseViewModel;
import com.iindicar.indicar.data.dao.BaseDao;
import com.iindicar.indicar.data.dao.BoardCommentDao;
import com.iindicar.indicar.data.dao.BoardDao;
import com.iindicar.indicar.data.dao.BoardFileDao;
import com.iindicar.indicar.data.dao.UserDao;
import com.iindicar.indicar.data.vo.BoardCommentVO;
import com.iindicar.indicar.data.vo.BoardDetailVO;
import com.iindicar.indicar.data.vo.BoardFileVO;
import com.iindicar.indicar.data.vo.UserVO;
import com.iindicar.indicar.utils.HttpClient;
import com.kakao.usermgmt.response.model.User;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.conn.ClientConnectionManager;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.protocol.HttpContext;

/**
 * Created by yeseul on 2018-04-13.
 *
 */

public class BoardDetailViewModel {
    private static final String TAG = BoardDetailViewModel.class.getSimpleName();

    public final ObservableBoolean isBoardDataLoading = new ObservableBoolean(true);
    public final ObservableBoolean isCommentDataLoading = new ObservableBoolean(true);

    private BoardDao boardDao;
    private BoardFileDao fileDao;
    private BoardCommentDao commentDao;
    private UserDao userDao;

    private BoardDetailNavigator navigator;

    public BoardDetailVO boardHeader;
    public String loginId;
    public String loginName;
    private int answerNo;

    private boolean isCommentUpdating = false;

    /** true: hide buttons */
    public final ObservableBoolean isKeyboardOpen = new ObservableBoolean(false);
    /** true: hide buttons, show fastUpButton */
    public final ObservableBoolean isPageUpScrolling = new ObservableBoolean(false);
    public final ObservableBoolean isScrolling = new ObservableBoolean(false);
    /** true: like button ON */
    public final ObservableBoolean isLikeBoard = new ObservableBoolean();
    /** bind with edit keyboard */
    public final ObservableField<String> commentWrite = new ObservableField<>();

    private static int doneCount = 0;

    public BoardDetailViewModel() {
        this.boardDao = new BoardDao();
        this.fileDao = new BoardFileDao();
        this.commentDao = new BoardCommentDao();
        this.userDao = new UserDao();
    }

    public void setNavigator(BoardDetailNavigator navigator) {
        this.navigator = navigator;
    }

    public void start() {
        Intent intent = navigator.getActivityIntent();
        Bundle bundle = intent.getBundleExtra("board");
        boardHeader = bundle.getParcelable("boardVO");
        this.loginId = intent.getStringExtra("loginId");
        this.loginName = intent.getStringExtra("loginName");

        checkIsLikeBoard();
        onRefreshBoard();
    }

    private void checkIsLikeBoard() {
        RequestParams params = new RequestParams();
        params.put("_id", loginId);
        boardDao.getLikeModel().getLikeList(params, new BaseDao.LoadDataListCallBack() {
            @Override
            public void onDataListLoaded(List list) {
                if(list == null){
                    isLikeBoard.set(false);
                    return;
                }

                for(int i = 0 ; i < list.size() ; i++){
                    BoardDetailVO vo = (BoardDetailVO) list.get(i);
                    if(boardHeader.getBoardType().equals(vo.getBoardType())
                            && boardHeader.getBoardId().equals(vo.getBoardId())){
                        isLikeBoard.set(true);
                        return;
                    }
                }
            }

            @Override
            public void onDataNotAvailable() {
                isLikeBoard.set(false);
            }
        });
    }

    public void onRefreshBoard() {

        isBoardDataLoading.set(true);

        getBoardData();
        getFileData();
        getCommentList();
    }

    public void getBoardData(){

        RequestParams params = new RequestParams();
        params.put("bbs_id", boardHeader.getBoardType());
        params.put("ntt_id", boardHeader.getBoardId());

        boardDao.getData(params, new BaseDao.LoadDataCallBack() {
            @Override
            public void onDataLoaded(Object data) {

                boardHeader = ((BoardDetailVO) data);

                getUser();
            }

            @Override
            public void onDataNotAvailable() {
                navigator.onBoardNotAvailable();
            }
        });
    }

    private void getUser() {
        RequestParams params = new RequestParams();
        params.put("_id", boardHeader.getUserId());

        userDao.getData(params, new BaseDao.LoadDataCallBack() {
            @Override
            public void onDataLoaded(Object data) {
                UserVO vo = (UserVO) data;
                boardHeader.setUserProfileUrl(vo.getProfileImageUrl());
                navigator.onHeaderAdded(boardHeader);
            }

            @Override
            public void onDataNotAvailable() {

            }
        });
    }

    private void getFileData() {

        final TreeMap<Integer, BoardFileVO> doneFile = new TreeMap<>();

        final String[] fileIndexArray = boardHeader.getAtchFileId();

        // 이미지가 없는 게시물
        if(fileIndexArray == null || fileIndexArray.length == 0){
            isBoardDataLoading.set(false);
            return;
        }

        for(int i = 0 ; i < fileIndexArray.length ; i++) {
            final int position = i;
            RequestParams params = new RequestParams();
            params.put("atch_file_id", fileIndexArray[i]);

            fileDao.getData(params, new BaseDao.LoadDataCallBack() {
                @Override
                public void onDataLoaded(Object data) {
                    BoardFileVO vo = (BoardFileVO) data;
                    doneFile.put(position, vo);

                    if(doneFile.size() == fileIndexArray.length) {
                        isBoardDataLoading.set(false);
                        for(BoardFileVO file : doneFile.values()){
                            navigator.onItemAdded(file);
                        }
                    }
                }

                @Override
                public void onDataNotAvailable() {
                    doneFile.put(position, null);
                }
            });
        }
    }

    private void getCommentList() {

        isCommentDataLoading.set(true);

        RequestParams params = new RequestParams();
        params.put("bbs_id", boardHeader.getBoardType());
        params.put("ntt_id", boardHeader.getBoardId());

        commentDao.getDataList(params, new BaseDao.LoadDataListCallBack() {
            @Override
            public void onDataListLoaded(List list) {
                navigator.onCommentUpdated(list);
                isCommentDataLoading.set(false);

                getUserProfile(list);
            }

            @Override
            public void onDataNotAvailable() {
                isCommentDataLoading.set(false);
            }
        });
    }

    private void getUserProfile(List list) {

        if(list == null){
            return;
        }

        for(int i = 0 ; i < list.size() ; i++){
            final BoardCommentVO comment = (BoardCommentVO) list.get(i);

            if(comment.getUserKey() == null){
                continue;
            }

            RequestParams params = new RequestParams();
            params.put("_id", comment.getUserKey());

            userDao.getData(params, new BaseDao.LoadDataCallBack() {
                @Override
                public void onDataLoaded(Object data) {
                    UserVO vo = (UserVO) data;
                    navigator.onCommentProfileAttached(comment, vo);
                }

                @Override
                public void onDataNotAvailable() {

                }
            });
        }

    }

    /* Data Binding
    *
    * 좋아요 버튼 onClick 메서드
    * */
    public boolean onLikeButtonClick(View view, MotionEvent event){

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            RequestParams params = new RequestParams();
            params.put("_id", loginId);
            params.put("bbs_id", boardHeader.getBoardType());
            params.put("ntt_id", boardHeader.getBoardId());

            boardDao.getLikeModel().toggleLike(params, new BaseDao.LoadDataCallBack() {
                @Override
                public void onDataLoaded(Object data) {
                    int likeCount = Integer.parseInt(boardHeader.getLikeCount());
                    if(isLikeBoard.get()) { // 좋아요 였는데 취소함
                        boardHeader.setLikeCount(String.valueOf(likeCount - 1));
                    } else {
                        boardHeader.setLikeCount(String.valueOf(likeCount + 1));
                    }
                    isLikeBoard.set(!isLikeBoard.get());
                }

                @Override
                public void onDataNotAvailable() {

                }
            });
        }
        return true;
    }

    public void onCommentSubmitClick(View view){
        navigator.hideKeyboard();

        if(commentWrite.get().equals("")){
            return;
        }

        if(isCommentUpdating)
            updateComment();
        else
            insertComment();
    }

    public void startCommentUpdating(BoardCommentVO vo) {
        isCommentUpdating = true;
        commentWrite.set(vo.getContent());
        answerNo = vo.getCommentIndex();
    }

    public void updateComment(){

        RequestParams params = new RequestParams();
        params.put("ntt_id", boardHeader.getBoardId());
        params.put("answer_no", answerNo);
        params.put("answer", commentWrite.get());

        commentDao.updateData(params, new BaseDao.LoadDataCallBack() {
            @Override
            public void onDataLoaded(Object data) {
                commentWrite.set("");
                Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isCommentUpdating = false;
                        getCommentList();
                    }
                }, 100);
            }
            @Override
            public void onDataNotAvailable() {
                isCommentUpdating = false;
            }
        });
    }

    public void insertComment(){
        RequestParams params = new RequestParams();

        params.put("bbs_id", boardHeader.getBoardType());
        params.put("ntt_id", boardHeader.getBoardId());
        params.put("answer", commentWrite.get());
        params.put("writer_nm", loginName);
        params.put("writer_id", loginId);

        commentDao.insertData(params, new BaseDao.LoadDataCallBack() {
            @Override
            public void onDataLoaded(Object data) {
                commentWrite.set("");
                Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isCommentUpdating = false;
                        getCommentList();
                    }
                }, 100);
            }
            @Override
            public void onDataNotAvailable() {

            }
        });
    }

    /* Data Binding
    *
    * 댓글 버튼의 onClick 메서드
    * */
    public boolean onCommentButtonClick(View view, MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            navigator.showKeyboard();
        }
        return true;
    }

    public void deleteComment(int index) {
        RequestParams params = new RequestParams();
        params.put("ntt_id", boardHeader.getBoardId());
        params.put("answer_no", index);

        commentDao.deleteData(params, new BaseDao.LoadDataCallBack() {
            @Override
            public void onDataLoaded(Object data) {
                getCommentList();
            }

            @Override
            public void onDataNotAvailable() {

            }
        });
    }

    public void deleteBoard() {
        RequestParams params = new RequestParams();
        params.put("bbs_id", this.boardHeader.getBoardType());
        params.put("ntt_id", this.boardHeader.getBoardId());

        boardDao.deleteData(params, new BaseDao.LoadDataCallBack() {
            @Override
            public void onDataLoaded(Object data) {
                navigator.onFinishActivity();
            }

            @Override
            public void onDataNotAvailable() {
                navigator.showTestToast("게시물 삭제 실패");
            }
        });
    }
}