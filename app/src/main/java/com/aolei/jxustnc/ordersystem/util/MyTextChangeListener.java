package com.aolei.jxustnc.ordersystem.util;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.view.View;

import com.aolei.jxustnc.ordersystem.R;

/**
 * 注册验证数据是否合法
 * Created by aolei on 2016/4/12.
 */
public class MyTextChangeListener implements View.OnFocusChangeListener {
    private TextInputLayout mTextInputLayout;
    private Context context;

    public MyTextChangeListener(TextInputLayout textInputLayout, Context context) {
        this.mTextInputLayout = textInputLayout;
        this.context = context;
    }

    /**
     * EditText 失去焦点监听事件
     *
     * @param v
     * @param hasFocus
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_register_username:
                if (!hasFocus) {
                }
                break;
            case R.id.et_register_pwd:
                if (!hasFocus) {
                    if (!CheckUtils.isPassWord(mTextInputLayout.getEditText().getText().toString())) {
                        Snackbar.make(mTextInputLayout.getEditText(), "密码必须是数字和字母的组合且长度必须大于8小于16位", Snackbar.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.et_register_phone:
                if (!hasFocus) {
                    if (!CheckUtils.isMobileNumber(mTextInputLayout.getEditText().getText().toString())) {
                        Snackbar.make(mTextInputLayout.getEditText(), "您输入的手机号码不符合规范", Snackbar.LENGTH_SHORT).show();
                    }
                }
                break;

            default:
                break;
        }
    }


}
