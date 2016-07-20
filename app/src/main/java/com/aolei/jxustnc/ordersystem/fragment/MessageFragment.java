package com.aolei.jxustnc.ordersystem.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aolei.jxustnc.ordersystem.R;
import com.aolei.jxustnc.ordersystem.activity.ShopMainActivity;
import com.aolei.jxustnc.ordersystem.adapter.RecyclerViewCommonAdapter;
import com.aolei.jxustnc.ordersystem.adapter.RecyclerViewViewHolder;
import com.aolei.jxustnc.ordersystem.entity.Order;
import com.aolei.jxustnc.ordersystem.httputil.BmobHttp;
import com.aolei.jxustnc.ordersystem.i.OnRefreshDataListener;
import com.aolei.jxustnc.ordersystem.util.TimeUtil;
import com.aolei.jxustnc.ordersystem.util.Utils;

import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 商家接收消息界面
 */
@SuppressLint("ValidFragment")
public class MessageFragment extends Fragment {

    private View view;
    private RecyclerView recyclerview_msg;
    private List<Order> data_list;
    private RecyclerViewCommonAdapter<Order> mAdapter;
    private SwipeRefreshLayout msg_swipe_layout;
    private boolean flag = false;
    private ShopMainActivity activity;
    private boolean isDeal = false;//查询是否处理订单
    private TextView tv_msg_no_order;

   public MessageFragment(){

   }


    public MessageFragment(boolean isDeal) {
        this.isDeal = isDeal;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_message, container, false);
        recyclerview_msg = (RecyclerView) view.findViewById(R.id.recyclerview_msg);
        msg_swipe_layout = (SwipeRefreshLayout) view.findViewById(R.id.msg_swipe_layout);
        tv_msg_no_order = (TextView) view.findViewById(R.id.tv_msg_no_order);
        Utils.showProgressDialog(getActivity());
        getNetData();
        initEvent();
        return view;
    }

    private void initEvent() {
        msg_swipe_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                flag = true;
                getNetData();
            }
        });
        if (!isDeal) {
            activity = (ShopMainActivity) getActivity();

            activity.setOnRefreshDataListener(new OnRefreshDataListener() {
                @Override
                public void onRefreshData() {
                    flag = true;
                    getNetData();
                }
            });
        }
    }

    /**
     * 从服务器获取数据
     */
    public void getNetData() {
        BmobHttp bmobHttp = new BmobHttp(getActivity());
        bmobHttp.queryOrderMsg(BmobUser.getCurrentUser(getActivity()).getObjectId(), isDeal, new FindListener<Order>() {
            @Override
            public void onSuccess(List<Order> list) {
                data_list = list;
                if (list.size() != 0) {
                    if (flag) {
                        mAdapter.notifyData(data_list);
                        msg_swipe_layout.setRefreshing(false);
                    } else {
                        showRecyclerView();
                    }
                    flag = false;
                } else {
                    tv_msg_no_order.setVisibility(View.VISIBLE);
                    Utils.closeProgressDialog();
                }
            }

            @Override
            public void onError(int i, String s) {
//                ToastUtil.showShort(getActivity(), "查询失败" + s);
                Log.d("MessageFragment", "查询订单失败" + s);
                Utils.closeProgressDialog();
            }
        });
    }

    /**
     * 设置RecyclerView
     */
    private void showRecyclerView() {
        final Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "iconfont.ttf");
        if (data_list != null && data_list.size() != 0) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerview_msg.setLayoutManager(linearLayoutManager);
            mAdapter = new RecyclerViewCommonAdapter<Order>(getActivity(), R.layout.item_msg_recyclerview, data_list) {
                @Override
                public void convert(final RecyclerViewViewHolder holder, final Order order) {
                    holder.setTextView(R.id.tv_msg_name, order.getFood().getName());
                    holder.setTextView(R.id.tv_msg_tel, order.getPhone());
                    TextView tv_status = holder.getView(R.id.tv_msg_status);
                    if (isDeal) {
                        holder.getView(R.id.btn_deal_order).setVisibility(View.GONE);
                        tv_status.setText(getResources().getString(R.string.tv_done_order_ttf));
                    }
                    tv_status.setTypeface(typeface);
                    holder.setTextView(R.id.tv_msg_dorm, order.getDorm());
                    holder.setTextView(R.id.tv_msg_time, TimeUtil.getFormatDate(order.getCreatedAt()));
                    holder.setTextView(R.id.tv_msg_count, order.getCount() + "份");
                    holder.setTextView(R.id.tv_total_money, "共计" + order.getMoney() + "元");

                    holder.getView(R.id.btn_msg_phone).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intentPhone = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + order.getUser().getMobilePhoneNumber()));
                            startActivity(intentPhone);
                        }
                    });
                    holder.getView(R.id.btn_deal_order).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("处理该订单?").setMessage("点击确认后，您需要处理该订单并送至客户。");
                            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dealOrder(order.getObjectId());
                                    mAdapter.deleteItem(mAdapter.getPosition(holder));
                                }
                            });
                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.create().show();
                        }
                    });
                }
            };
            recyclerview_msg.setAdapter(mAdapter);
            recyclerview_msg.setItemAnimator(new DefaultItemAnimator());
            mAdapter.setOnItemClickListener(new RecyclerViewCommonAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, final int position) {

                }
            });
            mAdapter.setmOnItemLongClickLisener(new RecyclerViewCommonAdapter.onItemLongClickLisener() {
                @Override
                public void onItemLongClick(View view, int position) {

                }
            });
        }
        msg_swipe_layout.setRefreshing(false);
        Utils.closeProgressDialog();
    }

    private void dealOrder(String objectId) {
        Order order = new Order();
        order.setDeal(true);
        order.update(getActivity(), objectId, new UpdateListener() {
            @Override
            public void onSuccess() {
                Log.d("NewOrin", "订单处理成功!");
            }

            @Override
            public void onFailure(int i, String s) {
                Log.d("NewOrin", "订单处理失败!" + s);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
//        BmobIM.getInstance().addMessageListHandler(this);
    }

    @Override
    public void onPause() {
        super.onPause();
//        BmobIM.getInstance().removeMessageListHandler(this);
    }
}
