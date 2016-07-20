/*
 * Copyright (c) 2016.
 * NewOrin 版权所有
 */

package com.aolei.jxustnc.ordersystem.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aolei.jxustnc.ordersystem.R;
import com.aolei.jxustnc.ordersystem.entity.Comment;
import com.aolei.jxustnc.ordersystem.entity.Food;
import com.aolei.jxustnc.ordersystem.httputil.BmobHttp;
import com.aolei.jxustnc.ordersystem.adapter.RecyclerViewCommonAdapter;
import com.aolei.jxustnc.ordersystem.adapter.RecyclerViewViewHolder;
import com.aolei.jxustnc.ordersystem.util.TimeUtil;
import com.aolei.jxustnc.ordersystem.util.ToastUtil;
import com.aolei.jxustnc.ordersystem.view.CircleImageView;
import com.bumptech.glide.Glide;

import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;

public class FoodDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsing_layout;
    private RecyclerView food_detail_recyclerview;
    private LinearLayout comment_layout;
    private TextView tv_price_item_food, tv_shop_item_food, tv_canteen_item_food, tv_sold_item_food, tv_ic_phone_call, tv_ic_edit_food, tv_food_com_count;
    private ImageView img_food;
    private Button btn_buy_food;
    private PercentRelativeLayout food_layout_canteen, food_layout_call;
    private String foodId;
    private List<Comment> data_list;
    private RecyclerViewCommonAdapter<Comment> mAdapter;
    private Food food;
    private String tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        tag = (String) BmobUser.getObjectByKey(this, "tag");
        initView();
        initEvent();
    }

    private void initEvent() {
        btn_buy_food.setOnClickListener(this);
        food_layout_call.setOnClickListener(this);
        food_layout_canteen.setOnClickListener(this);
    }

    private void initView() {
        btn_buy_food = (Button) findViewById(R.id.btn_buy_food);
        comment_layout = (LinearLayout) findViewById(R.id.comment_layout);
        tv_price_item_food = (TextView) findViewById(R.id.tv_price_item_food);
        tv_shop_item_food = (TextView) findViewById(R.id.tv_shop_item_food);
        tv_food_com_count = (TextView) findViewById(R.id.tv_food_com_count);
        tv_canteen_item_food = (TextView) findViewById(R.id.tv_canteen_item_food);
        tv_ic_phone_call = (TextView) findViewById(R.id.tv_ic_phone_call);
        tv_ic_edit_food = (TextView) findViewById(R.id.tv_ic_edit_food);
        tv_sold_item_food = (TextView) findViewById(R.id.tv_sold_item_food);
        food_detail_recyclerview = (RecyclerView) findViewById(R.id.food_detail_recyclerview);
        food_detail_recyclerview.setFocusable(false);
        img_food = (ImageView) findViewById(R.id.img_food);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsing_layout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_layout);
        food_layout_call = (PercentRelativeLayout) findViewById(R.id.food_layout_call);
        food_layout_canteen = (PercentRelativeLayout) findViewById(R.id.food_layout_canteen);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setData();
        /**
         * 设置立即购买Button是否隐藏
         */
        if (tag.equals("1")) {
            btn_buy_food.setVisibility(View.GONE);
            tv_ic_phone_call.setVisibility(View.GONE);
            tv_ic_edit_food.setVisibility(View.VISIBLE);
        } else {
            btn_buy_food.setVisibility(View.VISIBLE);
            tv_ic_phone_call.setVisibility(View.VISIBLE);
            tv_ic_edit_food.setVisibility(View.GONE);
        }
        initTypeFont();
    }

    private void initTypeFont() {
        Typeface typeface = Typeface.createFromAsset(getAssets(), "iconfont.ttf");
        tv_ic_edit_food.setTypeface(typeface);
        tv_ic_phone_call.setTypeface(typeface);
    }

    /**
     * 设置值
     */
    private void setData() {
        Bundle bundle = getIntent().getExtras();
        food = (Food) bundle.getSerializable("food");
        tv_price_item_food.setText(" ￥" + food.getPrice());
        tv_shop_item_food.setText(food.getStore().getStore_name());
        tv_canteen_item_food.setText("江西理工大学" + food.getStore().getBelong_cateen());
        if (food.getSold_count() == null) {
            tv_sold_item_food.setText("已售0份");
        } else {
            tv_sold_item_food.setText("已售" + food.getSold_count());
        }
        collapsing_layout.setTitle(food.getName());
        foodId = food.getObjectId();
        Glide.with(this).load(food.getImg_url()).error(R.drawable.imgloadfiald).into(img_food);
        getNetData();
    }

    /**
     * 获取网络数据
     */
    private void getNetData() {
        BmobHttp bmobHttp = new BmobHttp(this);
        bmobHttp.queryFoodCommment(foodId, new FindListener<Comment>() {
            @Override
            public void onSuccess(List<Comment> list) {
                data_list = list;
                setRecyclerView();
            }

            @Override
            public void onError(int i, String s) {
                Toast.makeText(FoodDetailActivity.this, "获取失败:" + s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 设置评论RecyclerView
     */
    private void setRecyclerView() {
        if (data_list.size() != 0 && data_list != null) {
            tv_food_com_count.setText(data_list.size() + "人评价");
            comment_layout.setVisibility(View.GONE);
            food_detail_recyclerview.setVisibility(View.VISIBLE);
            mAdapter = new RecyclerViewCommonAdapter<Comment>(this, R.layout.item_food_comment, data_list) {
                @Override
                public void convert(RecyclerViewViewHolder holder, Comment comment) {
                    holder.setTextView(R.id.tv_comment_name, comment.getUser().getUsername());
                    holder.setTextView(R.id.tv_comment_time, TimeUtil.getFormatDate(comment.getCreatedAt()));
                    holder.setTextView(R.id.tv_comment_content, comment.getContent());
                    CircleImageView img_comment_avatar = holder.getView(R.id.img_comment_avatar);
                    Glide.with(FoodDetailActivity.this).load(comment.getUser().getAvatar_url()).into(img_comment_avatar);
                }
            };
            food_detail_recyclerview.setAdapter(mAdapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            food_detail_recyclerview.setLayoutManager(linearLayoutManager);
            food_detail_recyclerview.setNestedScrollingEnabled(false);
            food_detail_recyclerview.setItemAnimator(new DefaultItemAnimator());
//            food_detail_recyclerview.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
            mAdapter.setOnItemClickListener(new RecyclerViewCommonAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {

                }
            });
            mAdapter.setmOnItemLongClickLisener(new RecyclerViewCommonAdapter.onItemLongClickLisener() {
                @Override
                public void onItemLongClick(View view, int position) {

                }
            });
        } else {
            tv_food_com_count.setText("暂无评价");
            comment_layout.setVisibility(View.VISIBLE);
            food_detail_recyclerview.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_buy_food:
                if (BmobUser.getCurrentUser(FoodDetailActivity.this) != null) {
                    Intent intent = new Intent(this, SubmitOrderActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("food", food);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                } else {
                    ToastUtil.showShort(FoodDetailActivity.this, "请先登录!");
                    startActivity(new Intent(FoodDetailActivity.this, LoginActivity.class));
                }
                break;
            case R.id.food_layout_call:
                if (tag.equals("0")) {
                    Intent intentPhone = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + food.getStore().getPhone_number()));
                    startActivity(intentPhone);
                } else {
                    Intent intent = new Intent(FoodDetailActivity.this, AddFoodActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("flag", false);
                    bundle.putSerializable("food", food);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.food_layout_canteen:
                break;
        }
    }
}
