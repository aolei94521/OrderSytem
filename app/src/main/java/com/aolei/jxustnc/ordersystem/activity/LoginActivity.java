package com.aolei.jxustnc.ordersystem.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aolei.jxustnc.ordersystem.R;
import com.aolei.jxustnc.ordersystem.entity.User;
import com.aolei.jxustnc.ordersystem.util.Utils;
import com.easemob.EMCallBack;
import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.SaveListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_login, btn_test;
    private EditText et_username;
    private EditText et_password;
    private TextView tv_register;
    private User user;
    private SharedPreferences mSharedPreferences;
    private TextInputLayout input_layout_username;
    private TextInputLayout input_layout_password;
    private String username;
    private String password;
    private Toolbar login_toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_login);
        super.onCreate(savedInstanceState);
        mSharedPreferences = getSharedPreferences("user_info", Context.MODE_APPEND);
        initView();
        initEvent();
    }

    private void initEvent() {
        tv_register.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        btn_test.setOnClickListener(this);
    }

    void initView() {
        login_toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        btn_login = (Button) findViewById(R.id.login_btn);
        btn_test = (Button) findViewById(R.id.btn_test);
        et_username = (EditText) findViewById(R.id.et_login_name);
        et_password = (EditText) findViewById(R.id.et_login_pwd);
        tv_register = (TextView) findViewById(R.id.regist_textview);
        input_layout_username = (TextInputLayout) findViewById(R.id.input_layout_name);
        input_layout_password = (TextInputLayout) findViewById(R.id.input_layout_pwd);

        login_toolbar.setTitleTextColor(Color.WHITE);
        login_toolbar.setTitle("用户登录");
        setSupportActionBar(login_toolbar);
    }

    /**
     * 登陆按钮注册事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        user = new User();
        switch (v.getId()) {
            case R.id.btn_test:
                testEaseMob();
                break;
            case R.id.login_btn:
                Utils.showProgressDialog(LoginActivity.this);
                username = et_username.getText().toString().trim();
                password = et_password.getText().toString().trim();
                if (!username.equals("") && !password.equals("")) {
                    user.setUsername(username);
                    user.setPassword(password);
                    user.login(this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            loginEaseMob((String) BmobUser.getObjectByKey(LoginActivity.this, "eseamob_pwd"));
                            String tag = (String) BmobUser.getObjectByKey(LoginActivity.this, "tag");
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString("username", et_username.getText().toString());
                            editor.putString("userpwd", et_password.getText().toString());
                            editor.putBoolean("status", true);
                            editor.commit();
                            if ("0".equals(tag)) {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            } else if ("1".equals(tag)) {
                                startActivity(new Intent(LoginActivity.this, ShopMainActivity.class));
                            }
                            finish();
                        }

                        @Override
                        public void onFinish() {
                            Utils.closeProgressDialog();
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            showSnackBar("账号或密码不正确");
                            Utils.closeProgressDialog();
                        }
                    });
                } else {
                    showSnackBar("用户名或密码不能为空");
                    Utils.closeProgressDialog();
                }
                break;
            case R.id.regist_textview:
                showChooseUserDialog();
                break;
            default:
                break;
        }
    }

    private void showSnackBar(String text) {
        Snackbar.make(btn_login, text, Snackbar.LENGTH_LONG).show();
    }

    private void testEaseMob() {
        username = et_username.getText().toString().trim();
        password = et_password.getText().toString().trim();
        Log.d("NewOrin", "用户名" + username + "密码" + password);
        new Thread(new Runnable() {
            public void run() {
                try {
                    // 调用sdk注册方法
                    EMChatManager.getInstance().createAccountOnServer(username, password);
                } catch (final EaseMobException e) {
                    //注册失败
                    int errorCode = e.getErrorCode();
                    if (errorCode == EMError.NONETWORK_ERROR) {
                        Toast.makeText(getApplicationContext(), "网络异常，请检查网络！", Toast.LENGTH_SHORT).show();
                    } else if (errorCode == EMError.USER_ALREADY_EXISTS) {
                        Toast.makeText(getApplicationContext(), "用户已存在！", Toast.LENGTH_SHORT).show();
                    } else if (errorCode == EMError.UNAUTHORIZED) {
                        Toast.makeText(getApplicationContext(), "注册失败，无权限！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "注册失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).start();
    }

    /**
     * 登录环信即时通讯
     */
    private void loginEaseMob(String ease_pwd) {
        EMChatManager.getInstance().login(username, ease_pwd, new EMCallBack() {//回调
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    public void run() {
//                        EMGroupManager.getInstance().loadAllGroups();
//                        EMChatManager.getInstance().loadAllConversations();
                        Log.d("easemob", "登录聊天服务器成功！");
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                Log.d("main", "登录聊天服务器失败！");
            }
        });
    }

    /**
     * 显示选择用户对话框
     */
    private void showChooseUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择用户类型");
        builder.setItems(new String[]{"普通用户", "商家用户"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                if (which == 0) {
                    intent.putExtra("flag", which);
                } else if (which == 1) {
                    intent.putExtra("flag", which);
                }
                intent.setClass(LoginActivity.this, com.aolei.jxustnc.ordersystem.activity.RegistActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
