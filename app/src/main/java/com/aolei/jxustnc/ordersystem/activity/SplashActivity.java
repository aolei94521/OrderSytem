package com.aolei.jxustnc.ordersystem.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.aolei.jxustnc.ordersystem.R;
import com.aolei.jxustnc.ordersystem.httputil.BmobHttp;

import cn.bmob.v3.BmobUser;

/**
 * APP欢迎界面
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (BmobUser.getCurrentUser(this) == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            String tag = (String) BmobUser.getObjectByKey(SplashActivity.this, "tag");
            Log.d("NewOrin", "当前用户" + BmobUser.getCurrentUser(this).getUsername());
            if ("0".equals(tag)) {
                startActivity(new Intent(this, MainActivity.class));
            } else if ("1".equals(tag)) {
                startActivity(new Intent(this, ShopMainActivity.class));
            }
        }
        finish();
    }
}
