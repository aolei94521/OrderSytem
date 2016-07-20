package com.aolei.jxustnc.ordersystem.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.aolei.jxustnc.ordersystem.R;
import com.aolei.jxustnc.ordersystem.adapter.RecyclerViewCommonAdapter;
import com.aolei.jxustnc.ordersystem.adapter.RecyclerViewViewHolder;
import com.aolei.jxustnc.ordersystem.entity.Food;
import com.aolei.jxustnc.ordersystem.entity.Order;
import com.aolei.jxustnc.ordersystem.entity.User;
import com.aolei.jxustnc.ordersystem.util.ToastUtil;
import com.aolei.jxustnc.ordersystem.util.Utils;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.SaveListener;

public class SubmitOrderActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private int count = 1;
    private Toolbar submit_toolbar;
    private List<Order> data_list;
    private Button btn_submit_order;
    private EditText et_dorm, et_submit_phone;
    private String total_price;
    private Food food;
    private Order order;
    private Spinner spinner_dorm;
    private List<String> list_spinner;
    private ArrayAdapter<String> adapter;
    private String dorm;
    private TextView tv_shop_item_submit, tv_canteen_item_submit, tv_food_item_submit, tv_price_item_submit, tv_food_count_submit, tv_money_item_submit, tv_minus_item_submit, tv_plus_item_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_order);
        Utils.showProgressDialog(this);
        initView();
        initEvent();
    }

    /**
     * 从上一个Activity获取数据
     */
    private void getIntentData() {
        Bundle bundle = getIntent().getExtras();
        Order order = new Order();
        food = (Food) bundle.getSerializable("food");
        order.setFood(food);
        order.setMoney(food.getPrice());
        order.setCount(1);
        data_list.add(order);
        startConversation(food);

        /**
         * 设置订单数据
         */
        tv_shop_item_submit.setText(food.getStore().getStore_name());
        tv_canteen_item_submit.setText(food.getStore().getBelong_cateen());
        tv_food_item_submit.setText(food.getName());
        tv_price_item_submit.setText(food.getPrice());
        tv_food_count_submit.setText(count + "");
        total_price = count * Integer.parseInt(food.getPrice()) + "";
        tv_money_item_submit.setText(total_price + "");
        btn_submit_order.setText("提交订单( ￥" + total_price + " )");
    }

    /**
     * 开启Bmob对话
     *
     * @param food
     */
    private void startConversation(Food food) {
        Utils.closeProgressDialog();
    }


    private void initView() {
        tv_shop_item_submit = (TextView) findViewById(R.id.tv_shop_item_submit);
        tv_canteen_item_submit = (TextView) findViewById(R.id.tv_canteen_item_submit);
        tv_food_item_submit = (TextView) findViewById(R.id.tv_food_item_submit);
        tv_price_item_submit = (TextView) findViewById(R.id.tv_price_item_submit);
        tv_food_count_submit = (TextView) findViewById(R.id.tv_food_count_submit);
        tv_money_item_submit = (TextView) findViewById(R.id.tv_money_item_submit);
        tv_minus_item_submit = (TextView) findViewById(R.id.tv_minus_item_submit);
        tv_plus_item_submit = (TextView) findViewById(R.id.tv_plus_item_submit);

        et_dorm = (EditText) findViewById(R.id.et_dorm);
        et_submit_phone = (EditText) findViewById(R.id.et_submit_phone);
        spinner_dorm = (Spinner) findViewById(R.id.spinner_dorm);
        btn_submit_order = (Button) findViewById(R.id.btn_submit_order);
        submit_toolbar = (Toolbar) findViewById(R.id.submit_toolbar);
        setSupportActionBar(submit_toolbar);
        submit_toolbar.setTitle("提交订单");
        submit_toolbar.setTitleTextColor(Color.WHITE);
        et_submit_phone.setText(BmobUser.getCurrentUser(this).getMobilePhoneNumber());//设置EditText默认值
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        data_list = new ArrayList<>();
        list_spinner = new ArrayList<>();
        for (int i = 1; i < 9; i++) {
            list_spinner.add(i + "栋");
        }
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list_spinner);
        // 3,adapter设置一个下拉列表样式
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);

        //4,spinner加载适配器
        spinner_dorm.setAdapter(adapter);

        //5,spinner设置监听器
        spinner_dorm.setOnItemSelectedListener(this);
        getIntentData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 发送订单消息
     */
    private void sendOrderMsg() {
        Utils.showProgressDialog(this);
        order = new Order();
        order.setUser(BmobUser.getCurrentUser(this, User.class));
        order.setStore_uid(food.getStore().getUser().getObjectId());
        order.setFood(food);
        order.setPhone(et_submit_phone.getText().toString());
        order.setDeal(false);
        order.setDorm(dorm + " " + et_dorm.getText().toString());
        order.setMoney(total_price);
        count = (Integer.parseInt(total_price)) / Integer.parseInt(food.getPrice());
        order.setCount(count);

        //获取到与聊天人的会话对象。参数username为聊天人的userid或者groupid，后文中的username皆是如此
        EMConversation conversation = EMChatManager.getInstance().getConversation(food.getStore().getUser().getUsername());
        Log.d("NewOrin", "聊天对象" + food.getStore().getUser().getUsername());
//创建一条文本消息
        EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
//设置消息body
        TextMessageBody txtBody = new TextMessageBody("消息...");
        message.addBody(txtBody);
//设置接收人
        message.setReceipt(food.getStore().getUser().getUsername());
//把消息加入到此会话对象中
        conversation.addMessage(message);
//发送消息
        EMChatManager.getInstance().sendMessage(message, new EMCallBack() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int i, String s) {
                Log.d("NewOrin", "消息发送失败" + s);
                Utils.closeProgressDialog();
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });

        saveOrder(order);
    }

    private void initEvent() {
        tv_minus_item_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count > 1) {
                    count--;
                    tv_food_count_submit.setText(count + "");
                    total_price = count * Integer.parseInt(food.getPrice()) + "";
                    tv_money_item_submit.setText(total_price + "");
                    btn_submit_order.setText("提交订单( ￥" + total_price + " )");
                }
            }
        });
        tv_plus_item_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                tv_food_count_submit.setText(count + "");
                total_price = count * Integer.parseInt(food.getPrice()) + "";
                tv_money_item_submit.setText(total_price + "");
                btn_submit_order.setText("提交订单( ￥" + total_price + " )");
            }
        });
        btn_submit_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmOrderDialog();
            }
        });
    }

    /**
     * 保存订单信息
     *
     * @param order
     */
    private void saveOrder(final Order order) {
        if (order == null) return;
        order.save(this, new SaveListener() {
            @Override
            public void onSuccess() {
                order.getFood().increment("sold_count");
                order.getFood().update(SubmitOrderActivity.this);
                ToastUtil.showShort(SubmitOrderActivity.this, "订单发送成功!");
                Utils.closeProgressDialog();
                finish();
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtil.showShort(SubmitOrderActivity.this, "订单保存失败!" + s);
                Utils.closeProgressDialog();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        dorm = adapter.getItem(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void confirmOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认提交订单?");
        builder.setMessage("订单提交后不可以取消，商家会将外卖送到宿舍楼下");
        builder.setPositiveButton("提交", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendOrderMsg();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
