package com.iindicar.indicar.data.vo;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.google.gson.annotations.SerializedName;
import com.iindicar.indicar.BR;

/**
 * Created by yeseul on 2018-04-12.
 */

public class BoardCommentVO extends BaseObservable {

    @SerializedName("profile_img_url")
    private String userProfileUrl = "";
    @SerializedName("bbs_id")
    private String boardType = "";
    @SerializedName("ntt_id")
    private String boardId = "";
    @SerializedName("comment_cn")
    private String content = "";
    @SerializedName("name")
    private String userName = "";
    @SerializedName("writer_id")
    private String userKey = "";
    @SerializedName("frst_time")
    private String firstTime = "";
    @SerializedName("last_updt_time")
    private String lastUpdateTime = "";
    @SerializedName("id")
    private int commentIndex;

    @Bindable
    public String getUserProfileUrl() {
        return userProfileUrl;
    }

    public void setUserProfileUrl(String userProfileUrl) {
        this.userProfileUrl = userProfileUrl;
        notifyPropertyChanged(BR.userProfileUrl);
    }

    @Bindable
    public String getBoardType() {
        return boardType;
    }

    public void setBoardType(String boardType) {
        this.boardType = boardType;
        notifyPropertyChanged(BR.boardType);
    }


    @Bindable
    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
        notifyPropertyChanged(BR.boardId);
    }

    @Bindable
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        notifyPropertyChanged(BR.content);
    }

    @Bindable
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        notifyPropertyChanged(BR.userName);
    }

    @Bindable
    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
        notifyPropertyChanged(BR.userKey);
    }

    @Bindable
    public String getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(String firstTime) {
        this.firstTime = firstTime;
        notifyPropertyChanged(BR.firstTime);
    }

    @Bindable
    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        notifyPropertyChanged(BR.lastUpdateTime);
    }

    @Bindable
    public int getCommentIndex() {
        return commentIndex;
    }

    public void setCommentIndex(int commentIndex) {
        this.commentIndex = commentIndex;
        notifyPropertyChanged(BR.commentIndex);
    }
}
