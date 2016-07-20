package com.aolei.jxustnc.ordersystem.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.aolei.jxustnc.ordersystem.R;
import com.aolei.jxustnc.ordersystem.adapter.RecyclerViewCommonAdapter;
import com.aolei.jxustnc.ordersystem.adapter.RecyclerViewViewHolder;
import com.aolei.jxustnc.ordersystem.entity.Comment;
import com.aolei.jxustnc.ordersystem.entity.Order;
import com.aolei.jxustnc.ordersystem.entity.User;
import com.aolei.jxustnc.ordersystem.httputil.BmobHttp;
import com.aolei.jxustnc.ordersystem.util.TimeUtil;
import com.aolei.jxustnc.ordersystem.util.ToastUtil;
import com.aolei.jxustnc.ordersystem.util.Utils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * 未完成订单Fragment
 * Created by NewOr on 2016/4/25.
 */
@SuppressLint("ValidFragment")
public class UserOrderfragment extends Fragment {

    private View view;
    private RecyclerView recyclerview_undone;
    private LinearLayout userorder_layout;
    private List<Order> data_list;
    private SwipeRefreshLayout order_swipe_layout;
    private RecyclerViewCommonAdapter<Order> mAdapter;
    private boolean isDeal;//订单是否完成
    private String com_msg;
    private boolean flag = false;

    public UserOrderfragment() {
    }


    public UserOrderfragment(boolean isDeal) {
        this.isDeal = isDeal;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_userorder, container, false);
        initView(view);
        initEvent();
        queryData();
        return view;
    }

    private void initEvent() {
        order_swipe_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                flag = true;
                queryData();
            }
        });
    }

    private void initView(View view) {
        data_list = new ArrayList<>();
        recyclerview_undone = (RecyclerView) view.findViewById(R.id.recyclerview_undone);
        order_swipe_layout = (SwipeRefreshLayout) view.findViewById(R.id.order_swipe_layout);
        userorder_layout = (LinearLayout) view.findViewById(R.id.userorder_layout);
        order_swipe_layout.post(new Runnable() {
            @Override
            public void run() {
                order_swipe_layout.setRefreshing(true);
            }
        });
    }

    /**
     * 查询数据
     */
    private void queryData() {
        BmobHttp bmobHttp = new BmobHttp(getActivity());
        bmobHttp.queryUserOrder(isDeal, new FindListener<Order>() {
            @Override
            public void onSuccess(List<Order> list) {
                data_list = list;
                if (flag) {
                    mAdapter.notifyData(data_list);
                    order_swipe_layout.setRefreshing(false);
                } else {
                    setRecyclerView();
                }
            }

            @Override
            public void onError(int i, String s) {
                order_swipe_layout.post(new Runnable() {
                    @Override
                    public void run() {
                        order_swipe_layout.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 设置RecyclerView
     */
    private void setRecyclerView() {
        if (data_list != null && data_list.size() != 0) {
            recyclerview_undone.setVisibility(View.VISIBLE);
            userorder_layout.setVisibility(View.GONE);
            mAdapter = new RecyclerViewCommonAdapter<Order>(getActivity(), R.layout.item_order_recyclerview, data_list) {
                @Override
                public void convert(RecyclerViewViewHolder holder, final Order order) {
                    ImageView imageView = holder.getView(R.id.img_food_undone);
                    Glide.with(getActivity()).load(order.getFood().getImg_url()).into(imageView);
                    if (order.getFood().getStore() != null) {
                        holder.setTextView(R.id.tv_order_shopname, order.getFood().getStore().getStore_name());
                    }
                    holder.setTextView(R.id.tv_order_name, order.getFood().getName());
                    holder.setTextView(R.id.tv_order_money, "共 " + order.getCount() + " 份  付款 " + order.getMoney() + " 元");
                    holder.setTextView(R.id.tv_order_time, TimeUtil.getFormatDate(order.getCreatedAt()));
                    if (!isDeal) {
                        holder.getView(R.id.tv_order_comment).setVisibility(View.GONE);
                        holder.setTextView(R.id.tv_order_buy, "商家正在处理");
                    } else {
                        holder.setTextView(R.id.tv_order_buy, "已完成");
                        holder.getView(R.id.tv_order_comment).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showCommentDialog(order);
                            }
                        });
                    }
                }
            };
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerview_undone.setLayoutManager(linearLayoutManager);
            recyclerview_undone.setItemAnimator(new DefaultItemAnimator());
            recyclerview_undone.setAdapter(mAdapter);

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
            userorder_layout.setVisibility(View.VISIBLE);
            recyclerview_undone.setVisibility(View.GONE);
        }
        order_swipe_layout.post(new Runnable() {
            @Override
            public void run() {
                order_swipe_layout.setRefreshing(false);
            }
        });
    }

    /**
     * 显示评价
     *
     * @param order
     */
    private void showCommentDialog(final Order order) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_edittext, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("评价");
        builder.setView(view);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utils.showProgressDialog(getActivity());
                EditText et_comment = (EditText) view.findViewById(R.id.et_info);
                com_msg = et_comment.getText().toString();
                Comment comment = new Comment();
                comment.setUser(BmobUser.getCurrentUser(getActivity(), User.class));
                comment.setContent(com_msg);
                comment.setFood(order.getFood());
                sendComment(comment);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    /**
     * 评价
     *
     * @param comment
     */
    private void sendComment(Comment comment) {
        BmobHttp bmobHttp = new BmobHttp(getActivity());
        bmobHttp.saveComment(comment, new SaveListener() {
            @Override
            public void onSuccess() {
                ToastUtil.showShort(getActivity(), "评价成功!");
                Utils.closeProgressDialog();
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtil.showShort(getActivity(), "评价失败!");
                Utils.closeProgressDialog();
            }
        });
    }
}
