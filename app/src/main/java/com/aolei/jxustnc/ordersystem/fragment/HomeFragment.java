package com.aolei.jxustnc.ordersystem.fragment;


import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.aolei.jxustnc.ordersystem.R;
import com.aolei.jxustnc.ordersystem.activity.AddFoodActivity;
import com.aolei.jxustnc.ordersystem.activity.FoodDetailActivity;
import com.aolei.jxustnc.ordersystem.entity.Food;
import com.aolei.jxustnc.ordersystem.entity.User;
import com.aolei.jxustnc.ordersystem.httputil.BmobHttp;
import com.aolei.jxustnc.ordersystem.httputil.NetworkUtil;
import com.aolei.jxustnc.ordersystem.adapter.RecyclerViewCommonAdapter;
import com.aolei.jxustnc.ordersystem.adapter.RecyclerViewViewHolder;
import com.aolei.jxustnc.ordersystem.util.ToastUtil;
import com.bumptech.glide.Glide;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private List<Food> data_list;
    private View view;
    private RecyclerView home_recyclerview;
    private SwipeRefreshLayout home_swipe_layout;
    private RecyclerViewCommonAdapter<Food> mAdapter;
    private String food_id;
    private Bundle savedState;
    private boolean flag = false;
    private String tag;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        initView();
        initEvent();
        return view;

    }

    private void initEvent() {
    }

    private void initView() {
        home_recyclerview = (RecyclerView) view.findViewById(R.id.home_recyclerview);
        home_swipe_layout = (SwipeRefreshLayout) view.findViewById(R.id.home_swipe_layout);
        home_swipe_layout.post(new Runnable() {
            @Override
            public void run() {
                home_swipe_layout.setRefreshing(true);
            }
        });
        home_swipe_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                flag = true;
                getNetData();
            }
        });
        getNetData();
    }

    /**
     * 获取网络数据
     */
    private void getNetData() {
        tag = (String) BmobUser.getObjectByKey(getActivity(), "tag");
        if (NetworkUtil.isConnected(getActivity())) {
            BmobHttp bmobHttp = new BmobHttp(getActivity());
            BmobQuery<Food> query = new BmobQuery<>();
            if (tag.equals("0")) {
                bmobHttp.queryFood(query, new FindListener<Food>() {
                    @Override
                    public void onSuccess(List<Food> list) {
                        data_list = list;
                        if (flag) {
                            mAdapter.notifyData(data_list);
                            home_swipe_layout.setRefreshing(false);
                        } else {
                            setRecyclerView();
                        }
                        flag = false;
                    }

                    @Override
                    public void onError(int i, String s) {
                        home_swipe_layout.setRefreshing(false);
                    }
                });
            } else if (tag.equals("1")) {
                bmobHttp.queryUserFood(query, BmobUser.getCurrentUser(getActivity()).getObjectId(), new FindListener<Food>() {
                    @Override
                    public void onSuccess(List<Food> list) {
                        data_list = list;
                        if (flag) {
                            mAdapter.notifyData(data_list);
                            home_swipe_layout.setRefreshing(false);
                        } else {
                            setRecyclerView();
                        }
                        flag = false;
                    }

                    @Override
                    public void onError(int i, String s) {
                        home_swipe_layout.setRefreshing(false);
                    }
                });
            }
        } else {
            ToastUtil.showLong(getActivity(), "网络尚未连接");
            home_swipe_layout.post(new Runnable() {
                @Override
                public void run() {
                    home_swipe_layout.setRefreshing(false);
                }
            });
        }
    }

    /**
     * 设置RecyclerView
     */
    private void setRecyclerView() {
        if (data_list.size() != 0 && data_list != null) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            home_recyclerview.setLayoutManager(linearLayoutManager);
            home_recyclerview.setItemAnimator(new DefaultItemAnimator());
//            home_recyclerview.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            mAdapter = new RecyclerViewCommonAdapter<Food>(getActivity(), R.layout.item_home_recyclerview, data_list) {
                @Override
                public void convert(RecyclerViewViewHolder holder, Food food) {
                    holder.setTextView(R.id.tv_shop_item_home, food.getStore().getStore_name());
                    holder.setTextView(R.id.tv_canteen_item_home, food.getStore().getBelong_cateen());
                    holder.setTextView(R.id.tv_name_item_home, food.getName());
                    holder.setTextView(R.id.tv_price_item_home, "￥" + food.getPrice());
                    if (food.getSold_count() == null) {
                        holder.setTextView(R.id.tv_count_item_home, "已售0份");
                    } else {
                        holder.setTextView(R.id.tv_count_item_home, "已售" + food.getSold_count() + "份");
                    }
                    ImageView img_item_home = holder.getView(R.id.img_item_home);
                    Glide.with(getActivity()).load(food.getImg_url()).error(R.drawable.imgloadfiald).into(img_item_home);//设置图片
                }
            };
            home_recyclerview.setAdapter(mAdapter);
        } else {
            Toast.makeText(getActivity(), "data_list为空", Toast.LENGTH_SHORT).show();
        }
        home_swipe_layout.post(new Runnable() {
            @Override
            public void run() {
                home_swipe_layout.setRefreshing(false);
            }
        });
        mAdapter.setOnItemClickListener(new RecyclerViewCommonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("food", data_list.get(position));
                Intent intent = new Intent(getActivity(), FoodDetailActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        mAdapter.setmOnItemLongClickLisener(new RecyclerViewCommonAdapter.onItemLongClickLisener() {
            @Override
            public void onItemLongClick(View view, int position) {
                if (tag.equals("1")) {
                    showChooseDialog(position);
                }
            }
        });
    }

    /**
     * 长按选择对话框
     *
     * @param position
     */
    private void showChooseDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(new String[]{"编辑", "删除"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 1) confirmDialog(position);
                else {
                    Intent intent = new Intent(getActivity(), AddFoodActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("flag", false);
                    bundle.putSerializable("food", data_list.get(position));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
        builder.setNegativeButton("取消", null).create().show();
    }

    /**
     * 确认删除对话框
     *
     * @param position
     */
    private void confirmDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("确认删除该菜品?");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteFood(position);
            }
        }).setNegativeButton("取消", null).create().show();
    }

    /**
     * @param position
     */
    private void deleteFood(final int position) {
        BmobHttp bmobHttp = new BmobHttp(getActivity());
        bmobHttp.deleteFood(data_list.get(position).getObjectId(), new DeleteListener() {
            @Override
            public void onSuccess() {
                mAdapter.deleteItem(position);
                ToastUtil.showShort(getActivity(), "删除成功!");
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtil.showShort(getActivity(), "删除失败,请重试!" + s);
            }
        });
    }
}
