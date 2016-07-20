package com.aolei.jxustnc.ordersystem.httputil;

import android.content.Context;
import android.util.Log;

import com.aolei.jxustnc.ordersystem.entity.Comment;
import com.aolei.jxustnc.ordersystem.entity.FeedBack;
import com.aolei.jxustnc.ordersystem.entity.Food;
import com.aolei.jxustnc.ordersystem.entity.Order;
import com.aolei.jxustnc.ordersystem.entity.Store;
import com.aolei.jxustnc.ordersystem.entity.User;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 访问网络类
 * Created by NewOr on 2016/4/21.
 */
public class BmobHttp {

    private Context mContext;

    public BmobHttp(Context context) {
        mContext = context;
    }

    /**
     * 查询所有食物方法
     *
     * @param query
     */
    public void queryFood(BmobQuery<Food> query, FindListener<Food> foodFindListener) {
        query.order("-sold_count");
        query.include("store.user");
        query.findObjects(mContext, foodFindListener);
    }

    /**
     * 根据商家Id查询食物
     *
     * @param query
     * @param objectId
     * @param foodFindListener
     */
    public void queryUserFood(BmobQuery<Food> query, String objectId, FindListener<Food> foodFindListener) {
        query.order("-sold_count");
        query.include("store.user");
        query.order("-createdAt");
        query.addWhereEqualTo("user", objectId);
        query.findObjects(mContext, foodFindListener);
    }

    /**
     * 查询食物评论
     *
     * @param foodId
     * @param findListener
     */
    public void queryFoodCommment(String foodId, FindListener<Comment> findListener) {
        BmobQuery<Comment> query = new BmobQuery<>();
        query.include("user,food");
        query.addWhereEqualTo("food", foodId);
        query.order("-createdAt");
        query.findObjects(mContext, findListener);
    }

    /**
     * 通过店名查找店内食物
     *
     * @param query
     * @param foodFindListener
     * @param store_objectId
     */
    public void queryFoodByStoreName(BmobQuery<Food> query, FindListener<Food> foodFindListener, String store_objectId) {
        Log.d("name", store_objectId);
        query.addWhereEqualTo("store", store_objectId);
        query.order("-createdAt");
        query.include("store.user");
        query.findObjects(mContext, foodFindListener);
    }

    /**
     * 查询商家订单信息
     *
     * @param listener
     */
    public void queryOrderMsg(String store_uid, boolean isDeal, FindListener<Order> listener) {
        BmobQuery<Order> query1 = new BmobQuery<>();
        query1.addWhereEqualTo("store_uid", store_uid);
        BmobQuery<Order> query2 = new BmobQuery<>();
        query2.addWhereEqualTo("isDeal", isDeal);
        List<BmobQuery<Order>> queries = new ArrayList<>();
        queries.add(query1);
        queries.add(query2);
        BmobQuery query = new BmobQuery();
        query.and(queries);
        query.include("food,user");
        query.order("-createdAt");
        query.findObjects(mContext, listener);
    }

    /**
     * 查询用户订单
     *
     * @param isDeal   订单是否完成
     * @param listener
     */
    public void queryUserOrder(Boolean isDeal, FindListener<Order> listener) {
        BmobQuery<Order> query1 = new BmobQuery<>();
        query1.addWhereEqualTo("user", BmobUser.getCurrentUser(mContext).getObjectId());
        BmobQuery<Order> query2 = new BmobQuery<>();
        query2.addWhereEqualTo("isDeal", isDeal);
        List<BmobQuery<Order>> queries = new ArrayList<>();
        queries.add(query1);
        queries.add(query2);
        BmobQuery query = new BmobQuery();
        query.and(queries);
        query.include("food.store,user");
        query.order("-createdAt");
        query.findObjects(mContext, listener);
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @param updateListener
     */
    public void updateUserinfo(User user, UpdateListener updateListener) {
        user.update(mContext, BmobUser.getCurrentUser(mContext).getObjectId(), updateListener);
    }

    /**
     * 更新商店信息
     *
     * @param store
     * @param objectId
     * @param updateListener
     */
    public void updateStoreinfo(Store store, String objectId, UpdateListener updateListener) {
        store.update(mContext, objectId, updateListener);
    }

    /**
     * 更新食物信息
     *
     * @param food
     * @param objectId
     * @param updateListener
     */
    public void updateFoodInfo(Food food, String objectId, UpdateListener updateListener) {
        food.update(mContext, objectId, updateListener);
    }

    /**
     * 保存评价
     *
     * @param comment
     * @param saveListener
     */
    public void saveComment(Comment comment, SaveListener saveListener) {
        if (comment != null) {
            comment.save(mContext, saveListener);
        }
    }

    /**
     * 查询每个食堂商店
     *
     * @param canteen
     * @param listener
     */
    public void queryStoreByCanteen(String canteen, FindListener<Store> listener) {
        BmobQuery<Store> query = new BmobQuery<>();
        query.addWhereEqualTo("belong_cateen", canteen);
        query.order("objectId");
        query.findObjects(mContext, listener);
    }

    /**
     * 查询商店信息
     *
     * @param objectId
     * @param listener
     */
    public void queryStore(String objectId, FindListener<Store> listener) {
        BmobQuery<Store> query = new BmobQuery<>();
        query.addWhereEqualTo("user", objectId);
        query.include("user");
        query.findObjects(mContext, listener);
    }

    /**
     * 修改密码
     *
     * @param old_pwd
     * @param new_pwd
     * @param listener
     */
    public void updatePwd(String old_pwd, String new_pwd, UpdateListener listener) {
        if (BmobUser.getCurrentUser(mContext) != null) {
            BmobUser.updateCurrentUserPassword(mContext, old_pwd, new_pwd, listener);
        }
    }

    /**
     * 删除食物
     *
     * @param objectId
     * @param deleteListener
     */
    public void deleteFood(String objectId, DeleteListener deleteListener) {
        Food food = new Food();
        food.setObjectId(objectId);
        food.delete(mContext, deleteListener);
    }

    /**
     * 用户反馈
     * @param message
     * @param listener
     */
    public void doFeedBack(String message, SaveListener listener) {
        FeedBack feedBack = new FeedBack();
        feedBack.setMessage(message);
        feedBack.save(mContext, listener);
    }
}
