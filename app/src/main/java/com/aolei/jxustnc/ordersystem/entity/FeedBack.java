package com.aolei.jxustnc.ordersystem.entity;

import cn.bmob.v3.BmobObject;

/**
 * 用户反馈实体类
 * Created by NewOr on 2016/5/30.
 */
public class FeedBack extends BmobObject {
    private String message;

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
