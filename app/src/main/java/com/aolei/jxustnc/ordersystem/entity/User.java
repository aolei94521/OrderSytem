package com.aolei.jxustnc.ordersystem.entity;

import cn.bmob.v3.BmobUser;

/**
 * Created by aolei on 2016/4/12.
 */
public class User extends BmobUser {

    private String avatar_url;
    private String trueName;
    private String dormitoryNumber;
    private String tag;//tag=1为商家用户,tag=0为普通用户
    private String store_name;
    private String eseamob_pwd;
    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public String getStore_name() {
        return store_name;
    }

    public String getTrueName() {
        return trueName;
    }

    public void setTrueName(String trueName) {
        this.trueName = trueName;
    }

    public String getDormitoryNumber() {
        return dormitoryNumber;
    }

    public void setDormitoryNumber(String dormitoryNumber) {
        this.dormitoryNumber = dormitoryNumber;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public String getEseamob_pwd() {
        return eseamob_pwd;
    }

    public void setEseamob_pwd(String eseamob_pwd) {
        this.eseamob_pwd = eseamob_pwd;
    }
}
