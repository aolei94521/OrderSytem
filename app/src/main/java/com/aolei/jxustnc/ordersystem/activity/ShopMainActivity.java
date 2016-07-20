package com.aolei.jxustnc.ordersystem.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.aolei.jxustnc.ordersystem.R;
import com.aolei.jxustnc.ordersystem.fragment.HomeFragment;
import com.aolei.jxustnc.ordersystem.fragment.MessageFragment;
import com.aolei.jxustnc.ordersystem.fragment.UserinfoFragment;
import com.aolei.jxustnc.ordersystem.i.OnRefreshDataListener;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;

import java.util.ArrayList;
import java.util.List;

public class ShopMainActivity extends AppCompatActivity {

    private List<Fragment> mFragments;
    private FragmentPagerAdapter fragmentPagerAdapter;
    private ViewPager mViewPager;
    private Toolbar show_toolbar;
    private String[] mTabs = {"我的订单", "我的商店", "我的信息"};
    private TabLayout tabLayout;
    private NewMessageBroadcastReceiver msgReceiver;
    private NotificationManager manager;// 通知控制类
    private OnRefreshDataListener onRefreshDataListener;
    private FloatingActionButton mAddBtn;
    public static UserinfoFragment userinfoFragment;
    public void setOnRefreshDataListener(OnRefreshDataListener onRefreshDataListener) {
        this.onRefreshDataListener = onRefreshDataListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_main);
        msgReceiver = new NewMessageBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
        intentFilter.setPriority(3);
        registerReceiver(msgReceiver, intentFilter);
        manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        initView();
        initEvent();
    }

    private void initEvent() {
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopMainActivity.this, AddFoodActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("flag", true);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setBackground(getResources().getDrawable(R.color.colorPrimary));
        tabLayout.setTabTextColors(Color.parseColor("#A7D5EF"), Color.WHITE);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setSelectedTabIndicatorColor(Color.RED);
        tabLayout.setSelectedTabIndicatorHeight(10);

        mViewPager = (ViewPager) findViewById(R.id.container);
        mAddBtn = (FloatingActionButton) findViewById(R.id.fab);

        mFragments = new ArrayList<>();
        userinfoFragment = new UserinfoFragment();
        mFragments.add(new MessageFragment());
        mFragments.add(new HomeFragment());
        mFragments.add(userinfoFragment);

        show_toolbar = (Toolbar) findViewById(R.id.show_toolbar);
        show_toolbar.setTitleTextColor(Color.WHITE);
        show_toolbar.setTitle("商家");
        setSupportActionBar(show_toolbar);
        mViewPager.setOffscreenPageLimit(2);
        fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mTabs.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTabs[position];
            }
        };
        mViewPager.setAdapter(fragmentPagerAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 2) {
                    mAddBtn.setVisibility(View.GONE);
                } else {
                    mAddBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(msgReceiver);
    }

    public void setViewPager(int position) {
        Log.d("NewOrin", "刷新" + position);
        if (mViewPager != null) {
            mViewPager.setCurrentItem(position);
        }
    }

    public class NewMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 注销广播
            abortBroadcast();
            // 消息id（每条消息都会生成唯一的一个id，目前是SDK生成）
            String msgId = intent.getStringExtra("msgid");
            //发送方
            String username = intent.getStringExtra("from");
            // 收到这个广播的时候，message已经在db和内存里了，可以通过id获取message对象
            EMMessage message = EMChatManager.getInstance().getMessage(msgId);
            Log.d("NewOrin", "接到了消息");
            sendNotification();
            onRefreshDataListener.onRefreshData();
        /*EMConversation conversation = EMChatManager.getInstance().getConversation(username);
        // 如果是群聊消息，获取到group id
        if (message.getChatType() == EMMessage.ChatType.GroupChat) {
            username = message.getTo();
        }
        if (!username.equals(username)) {
            // 消息不是发给当前会话，return
            return;
        }*/
        }
    }

    private void sendNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        Intent intent = new Intent(this, ShopMainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.mipmap.ic_logo);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_logo));
        builder.setAutoCancel(true);
        builder.setContentTitle("新的订单");
        builder.setContentText("您有一条新的订单");
        builder.setDefaults(Notification.DEFAULT_ALL); // 以上三种都有
        //设置点击跳转
        Intent hangIntent = new Intent();
        hangIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        hangIntent.setClass(this, ShopMainActivity.class);
        //如果描述的PendingIntent已经存在，则在产生新的Intent之前会先取消掉当前的
        PendingIntent hangPendingIntent = PendingIntent.getActivity(this, 0, hangIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setFullScreenIntent(hangPendingIntent, true);
        manager.notify(0, builder.build());
    }
}
